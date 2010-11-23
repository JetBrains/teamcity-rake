/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;

/**
 * @author Roman.Chernyatchik
 */
public class RubySDKUtil {

  private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+(\\.[0-9A-z]+)*)");


  @NotNull
  public static Pair<String, String> findGemRootFolderAndVersion(final String gemName,
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
        public boolean accept(final File file) {
          // accept only versions of our given gem
          return file.getName().startsWith(gemNamePrefix) && file.isDirectory();
        }
      });

      // find gem with highest version or our forced version
      for (File gem : candidateGems) {
        final String dirtyVersion = gem.getName().substring(gemNamePrefix.length());
        // TODO: will not work with bundler git gems!
        // TODO at the moment not critical for test-unit and bundler in real life
        final Matcher matcher = VERSION_PATTERN.matcher(dirtyVersion);

        final String version;
        if (!matcher.find()) {
          final String msg = "Cannot determine gem version: " + gemNamePrefix + dirtyVersion
                             + "'(" + gemPath + ") gem. Please submit a feature request.";
          throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
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

  public static void failIfWithErrors(final RubyScriptRunner.Output result)
    throws RakeTasksBuildService.MyBuildFailureException {
    // script wasn't found in LOAD_PATH:
    if (!StringUtil.isEmpty(result.getStderr())) {
      throw new RakeTasksBuildService.MyBuildFailureException(result.getStdout() + "\n" + result.getStderr(),
                                                              RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }

    if (result.getStdout().contains("JAVA_HOME")) {
      throw new RakeTasksBuildService.MyBuildFailureException(result.getStdout(),
                                                              RakeRunnerBundle.RUNNER_ERROR_TITLE_JRUBY_PROBLEMS_IN_CONF_ON_AGENT);
    }
  }

  public static RubyScriptRunner.Output executeScriptFromSource(@NotNull final RubySdk sdk,
                                                                 @Nullable final Map<String, String> buildConfEnvironment,
                                                                 final String scriptSource,
                                                                 final String... rubyArgs) {

    return RubyScriptRunner.runScriptFromSource(sdk, rubyArgs, scriptSource, new String[0], buildConfEnvironment);
  }
}
