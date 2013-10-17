/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.rakerunner.ModifiableRunnerContext;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.SdkUtil;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.VersionComparatorUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;

/**
 * @author Roman.Chernyatchik
 */
public class RubySDKUtil {

  private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+(\\.[0-9A-z]+)*)");
  @NotNull
  public static final String GET_GEM_PATHS_SCRIPT = "require 'rubygems'; puts Gem.path";


  /**
   * Returns pair of gem path and version or null if not found
   */
  @Nullable
  public static Pair<String, String> findGemRootFolderAndVersion(@NotNull final String gemName,
                                                                 @NotNull final String[] gemPaths,
                                                                 @Nullable final String forcedGemVersion)
    throws RakeTasksBuildService.MyBuildFailureException {

    final List<Pair<String, String>> gems = findGemsByName(gemName, gemPaths);

    if (forcedGemVersion != null) {
      return CollectionsUtil.findFirst(gems, new Filter<Pair<String, String>>() {
        public boolean accept(@NotNull final Pair<String, String> data) {
          return forcedGemVersion.equals(data.second);
        }
      });
    }
    if (gems.isEmpty()) {
      return null;
    }
    return Collections.max(gems, new GemInfoPairComparator());
  }

  @NotNull
  public static List<Pair<String, String>> findGemsByName(@NotNull final String gemName,
                                                          @NotNull final String[] gemPaths)
    throws RakeTasksBuildService.MyBuildFailureException {

    final List<Pair<String, String>> found = new ArrayList<Pair<String, String>>();

    // look for our gem in gem paths
    for (String gemPath : gemPaths) {
      final String gemsFolderPath = toSystemIndependentName(gemPath + File.separatorChar + "gems");

      // gem path file may not exist
      if (!FileUtil2.checkIfDirExists(gemsFolderPath)) {
        continue;
      }

      // collect all version of our gem
      final String gemNamePrefix = gemName + "-";

      final File gemsFolder = new File(gemsFolderPath);
      final File[] candidateGems = gemsFolder.listFiles(new FileFilter() {
        public boolean accept(@NotNull final File file) {
          // accept only versions of our given gem
          return file.getName().startsWith(gemNamePrefix) && file.isDirectory();
        }
      });

      // find gem with highest version or our forced version
      for (File gem : candidateGems) {
        final String dirtyVersion = gem.getName().substring(gemNamePrefix.length());
        // TODO: will not work with bundler git gems!
        // at the moment not critical for test-unit and bundler in real life
        final Matcher matcher = VERSION_PATTERN.matcher(dirtyVersion);

        final String version;
        if (!matcher.find()) {
          final String msg = "Cannot determine gem version: " + gemNamePrefix + dirtyVersion
                             + "'(" + gemPath + ") gem. Please submit a feature request.";
          throw new RakeTasksBuildService.MyBuildFailureException(msg);
        }
        version = matcher.group();

        found.add(new Pair<String, String>(gem.getPath(), version));
      }
    }
    return found;
  }

  @Nullable
  public static String getForcedGemVersion(@NotNull final String forcedVersionProperty,
                                           @NotNull final Map<String, String> buildParameters) {
    final String customGemVersionProperty = buildParameters.get(forcedVersionProperty);

    return !StringUtil.isEmpty(customGemVersionProperty) ? customGemVersionProperty.trim() : null;
  }

  public static void failIfWithErrors(@NotNull final ExecResult result)
      throws RakeTasksBuildService.MyBuildFailureException {
    // script wasn't found in LOAD_PATH:
    //noinspection ThrowableResultOfMethodCallIgnored
    if (result.getExitCode() != 0 || result.getException() != null) {
      throw new RakeTasksBuildService.MyBuildFailureException(result.toString());
    }

    if (result.getStdout().contains("JAVA_HOME")) {
      throw new RakeTasksBuildService.MyBuildFailureException(result.getStdout());
    }
  }

  @NotNull
  public static RubySdk createAndSetupSdk(@NotNull final Map<String, String> runParameters,
                                          @NotNull final BuildRunnerContext context)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    // create
    return setupSdk(context, InternalRubySdkUtil.createSdk(runParameters, context));
  }

  @NotNull
  private static RubySdk setupSdk(@NotNull final BuildRunnerContext context,
                                  @NotNull final RubySdk sdk) {

    sdk.setup(context.getBuildParameters().getEnvironmentVariables());
    return sdk;
  }

  public static void patchPathEnvForNonRvmOrSystemRvmSdk(@NotNull final RubySdk sdk,
                                                         @NotNull final ModifiableRunnerContext context)
  throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    if (SdkUtil.isRvmSdk(sdk) && !sdk.isSystem()) {
      // do nothing
      return;
    }

    // Better to add sdk & gemsets bin folders to PATH. If bundler emulation is enabled
    // and bundler overrides gem paths - these alternative paths should be used

    final StringBuilder patchedPath = new StringBuilder();

    // sdk bin folder
    final File sdkBinFolder = sdk.getRubyExecutable().getParentFile();
    if (sdkBinFolder != null) {
      try {
        patchedPath.append(jetbrains.buildServer.util.FileUtil.toSystemDependentName(sdkBinFolder.getCanonicalPath()));
      } catch (IOException e) {
        throw new RunBuildException(e);
      }
    }

    // gempath bin folders
    // use bundler gems root if it is defined! (i.e. we use bundle exec emulation with custom gem paths)
    final String bundlerGemRoot = BundlerUtil.determineGemsRootsAccordingToBundlerSettings(sdk, context);
    final String[] gemPaths = bundlerGemRoot == null ? sdk.getGemPaths() : new String[]{bundlerGemRoot};
    // add to path
    for (String gemPath : gemPaths) {
      final String binFolderPath = jetbrains.buildServer.util.FileUtil.toSystemDependentName(gemPath + "/bin");
      patchedPath.append(File.pathSeparatorChar).append(binFolderPath);
    }

    // add old $PATH
    OSUtil.prependToPATHEnvVariable(patchedPath.toString(), context.getEnvParameters());
  }

  public static class GemInfoPairComparator implements Comparator<Pair<String, String>> {
    public int compare(@NotNull final Pair<String, String> o1, @NotNull final Pair<String, String> o2) {
      return VersionComparatorUtil.compare(o1.second, o2.second);
    }
  }
}
