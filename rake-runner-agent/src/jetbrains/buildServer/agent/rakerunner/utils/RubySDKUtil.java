/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.ruby.RubyLightweightSdk;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.impl.RubySdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubyLightweightSdk;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRubySdkImpl;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
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


  @NotNull
  public static Pair<String, String> findGemRootFolderAndVersion(@NotNull final String gemName,
                                                                 @NotNull final String[] gemPaths,
                                                                 @Nullable final String forcedGemVersion)
      throws RakeTasksBuildService.MyBuildFailureException {

    String ourGemVersion = null;
    String ourGemPath = null;

    // look for our gem in gem paths
    for (String gemPath : gemPaths) {
      if (forcedGemVersion != null && ourGemPath != null) {
        // our gem was found in previous gems path
        break;
      }

      final String gemsFolderPath = toSystemIndependentName(gemPath + File.separatorChar + "gems");

      // gem path file may not exist
      if (!FileUtil.checkIfDirExists(gemsFolderPath)) {
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

        if (forcedGemVersion != null) {
          // forced version
          if (version.equals(forcedGemVersion)) {
            // success!
            ourGemVersion = version;
            ourGemPath = gem.getPath();
            break;
          }
        } else {
          // determine latest version
          if (ourGemVersion == null || VersionComparatorUtil.compare(ourGemVersion, version) < 0) {
            ourGemVersion = version;
            ourGemPath = gem.getPath();
          }
        }
      }
    }

    return new Pair<String, String>(ourGemPath, ourGemVersion);
  }

  @Nullable
  public static String getForcedGemVersion(@NotNull final String forcedVersionProperty,
                                           @NotNull final Map<String, String> buildParameters) {
    final String customGemVersionProperty = buildParameters.get(forcedVersionProperty);

    return !StringUtil.isEmpty(customGemVersionProperty) ? customGemVersionProperty.trim()
        : null;
  }

  public static void failIfWithErrors(@NotNull final RubyScriptRunner.Output result)
      throws RakeTasksBuildService.MyBuildFailureException {
    // script wasn't found in LOAD_PATH:
    if (!StringUtil.isEmpty(result.getStderr())) {
      throw new RakeTasksBuildService.MyBuildFailureException(result.getStdout() + "\n" + result.getStderr());
    }

    if (result.getStdout().contains("JAVA_HOME")) {
      throw new RakeTasksBuildService.MyBuildFailureException(result.getStdout());
    }
  }

  @NotNull
  public static RubyScriptRunner.Output executeScriptFromSource(@NotNull final RubySdk sdk,
                                                                @Nullable final Map<String, String> buildConfEnvironment,
                                                                @NotNull final String scriptSource,
                                                                @NotNull final String... rubyArgs) {

    return RubyScriptRunner.runScriptFromSource(sdk, rubyArgs, scriptSource, new String[0], buildConfEnvironment);
  }

  @NotNull
  public static RubyLightweightSdk createAndSetupLightweightSdk(@NotNull final Map<String, String> runParameters,
                                                                @NotNull final BuildParametersMap buildParametersMap)
      throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    // create
    return InternalRubySdkUtil.createLightWeightSdk(runParameters, buildParametersMap.getAllParameters());
  }

  @NotNull
  public static RubySdk createAndSetupSdk(@NotNull final Map<String, String> runParameters,
                                          @NotNull final BuildParametersMap buildParametersMap)
      throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    return createAndSetupSdk(runParameters, buildParametersMap, createAndSetupLightweightSdk(runParameters, buildParametersMap));
  }

  @NotNull
  public static RubySdk createAndSetupSdk(@NotNull final Map<String, String> runParameters,
                                          @NotNull final BuildParametersMap buildParametersMap,
                                          @NotNull final RubyLightweightSdk lwSdk)
      throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    final Map<String, String> buildConfEnvironment = buildParametersMap.getEnvironmentVariables();

    // create
    // TODO: bundler gem paths!

    final RubySdk sdk;

    if (lwSdk.isRvmSdk() && !lwSdk.isSystem()) {
      // There a specific algorithm for true rvm rubies
      RVMRubySdkImpl sdkImpl = new RVMRubySdkImpl((RVMRubyLightweightSdk) lwSdk);
      sdk = sdkImpl;
      // initialize

      // language level
      sdkImpl.setIsRuby19(InternalRubySdkUtil.isRuby19Interpreter(sdk, buildConfEnvironment));

      // load path
      sdkImpl.setLoadPathsLog(InternalRubySdkUtil.getLoadPaths(sdk, buildConfEnvironment, sdkImpl.isRuby19()));

      // Other already initialized
    } else {
      RubySdkImpl sdkImpl = new RubySdkImpl(lwSdk);
      sdk = sdkImpl;
      // initialize:

      // language level
      sdkImpl.setIsRuby19(InternalRubySdkUtil.isRuby19Interpreter(sdk, buildConfEnvironment));

      // ruby / jruby
      sdkImpl.setIsJRuby(InternalRubySdkUtil.isJRubyInterpreter(sdk, buildConfEnvironment));

      // gem paths
      sdkImpl.setGemPathsLog(executeScriptFromSource(sdk, buildConfEnvironment, GET_GEM_PATHS_SCRIPT));

      // load path
      sdkImpl.setLoadPathsLog(InternalRubySdkUtil.getLoadPaths(sdk, buildConfEnvironment, sdkImpl.isRuby19()));
    }

    return sdk;
  }

  public static void patchPathEnvForNonRvmOrSystemRvmSdk(@NotNull final RubySdk sdk,
                                                         @NotNull final Map<String, String> runParams,
                                                         @NotNull final Map<String, String> buildParams,
                                                         @NotNull final Map<String, String> runnerEnvParams,
                                                         @Nullable final String checkoutDirPath)
      throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    if (sdk.isRvmSdk() && !sdk.isSystem()) {
      // do nothing
      return;
    }

    // Better to add sdk & gemsets bin folders to PATH. If bundler emulation is enabled
    // and bundler overrides gem paths - these alternative paths should be used

    final StringBuilder patchedPath = new StringBuilder();

    // sdk bin folder
    final String interpreterPath = sdk.getInterpreterPath();
    final File sdkBinFolder = new File(interpreterPath).getParentFile();
    if (sdkBinFolder != null) {
      try {
        patchedPath.append(jetbrains.buildServer.util.FileUtil.toSystemDependentName(sdkBinFolder.getCanonicalPath()));
      } catch (IOException e) {
        throw new RunBuildException(e);
      }
    }

    // gempath bin folders
    // use bundler gems root if it is defined! (i.e. we use bundle exec emulation with custom gem paths)
    final String bundlerGemRoot = BundlerUtil.determineGemsRootsAccordingToBundlerSettings(sdk,
        runParams, buildParams,
        runnerEnvParams,
        checkoutDirPath);
    final String[] gemPaths = bundlerGemRoot == null ? sdk.getGemPaths() : new String[]{bundlerGemRoot};
    // add to path
    for (String gemPath : gemPaths) {
      final String binFolderPath = jetbrains.buildServer.util.FileUtil.toSystemDependentName(gemPath + "/bin");
      patchedPath.append(File.pathSeparatorChar).append(binFolderPath);
    }

    // add old $PATH
    OSUtil.prependToPATHEnvVariable(patchedPath.toString(), runnerEnvParams);
  }
}
