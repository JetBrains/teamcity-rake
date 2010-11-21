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

import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.VersionComparatorUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import static jetbrains.buildServer.rakerunner.RakeRunnerConstants.TEST_UNIT_GEM_VERSION_PROPERTY;

/**
 * @author Roman.Chernyatchik
 */
public class RubySDKUtil {
  @NonNls
  public static final String AUTORUNNER_SCRIPT_PATH = "test/unit/autorunner.rb";
  @NonNls
  public static final String TESTRUNNERMEDIATOR_SCRIPT_PATH = "test/unit/ui/testrunnermediator.rb";

  private static final String TEST_UNIT_GEM_SUFFIX = "test-unit-";
  private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+(\\.[0-9A-z]+)*)");

  @NotNull
  public static String getSDKTestUnitAutoRunnerScriptPath(@NotNull final RubySdk sdk,
                                                          @NotNull final Map<String, String> buildParameters)
      throws RakeTasksBuildService.MyBuildFailureException {

    return findTestUnitScript(sdk, buildParameters, AUTORUNNER_SCRIPT_PATH);
  }

  @NotNull
  public static String getSDKTestUnitTestRunnerMediatorScriptPath(@NotNull final RubySdk sdk,
                                                                  @NotNull final Map<String, String> buildParameters)
      throws RakeTasksBuildService.MyBuildFailureException {

    return findTestUnitScript(sdk, buildParameters, TESTRUNNERMEDIATOR_SCRIPT_PATH);
  }

  /**
   * Finds script with given relative path in test-unit gem or Test::Unit framework bundled in SDK
   * @param buildParameters Build params
   * @param scriptPath Path of given script
   * @return Full path of given script
   * @throws jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService.MyBuildFailureException If script will not be found
   * @throws RunBuildException Other error
   */
  private static String findTestUnitScript(@NotNull final RubySdk sdk,
                                           final Map<String, String> buildParameters,
                                           final String scriptPath) throws RakeTasksBuildService.MyBuildFailureException {

    // At first let's try to find script in "test-unit" gem
    // then in sdk load path

    String testUnitGemVersion = null;
    String testUnitGemPath = null;

    final String customTestUnitGemVersionProperty = buildParameters.get(TEST_UNIT_GEM_VERSION_PROPERTY);
    final String customTestUnitGemVersion = !StringUtil.isEmpty(customTestUnitGemVersionProperty)
                                            ? customTestUnitGemVersionProperty.trim()
                                            : null ;

    // TODO: special value for test-unit gem version : [sdk] - force to use framework bundled in sdk

    // look for "test-unit" gems in gem paths
    for (String gemPath : sdk.getGemPaths()) {
      if (customTestUnitGemVersion != null && testUnitGemPath != null) {
        // test-unit gem was found in previous gems path
        break;
      }

      final String gemsFolderPath = toSystemIndependentName(gemPath + File.separatorChar + "gems");

      // gem path file may not exist
      if (!FileUtil.checkIfDirExists(gemsFolderPath)) {
        continue;
      }

      // collect test-unit gems
      final File gemsFolder = new File(gemsFolderPath);
      final File[] testUnitGems = gemsFolder.listFiles(new FileFilter() {
        public boolean accept(final File file) {
          // accept only test-unit gems
          return file.getName().startsWith(TEST_UNIT_GEM_SUFFIX) && file.isDirectory();
        }
      });

      // find gem with highest version
      for (File gem : testUnitGems) {
        final String dirtyVersion = gem.getName().substring(TEST_UNIT_GEM_SUFFIX.length());
        final Matcher matcher = VERSION_PATTERN.matcher(dirtyVersion);

        final String version;
        if (!matcher.find()) {
          final String msg = "Cannot determine gem version: " + TEST_UNIT_GEM_SUFFIX + testUnitGemVersion
                             + "'(" + testUnitGemPath + ") gem. Please submit a feature request.";
          throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
        }
        version = matcher.group();

        if (customTestUnitGemVersion != null) {
          // custom version
          if (version.equals(customTestUnitGemVersion)) {
            testUnitGemVersion = version;
            testUnitGemPath = gem.getPath();
            break;
          }
        } else {
          // determine latest version
          if (testUnitGemVersion == null || VersionComparatorUtil.compare(testUnitGemVersion, version) < 0) {
            testUnitGemVersion = version;
            testUnitGemPath = gem.getPath();
          }
        }
      }
    }

    if (testUnitGemPath != null) {
      final String fullScriptPath = testUnitGemPath + File.separatorChar + "lib" + File.separatorChar + scriptPath;
      if (FileUtil.checkIfExists(fullScriptPath)) {
        return fullScriptPath;
      } else {

        // Error: Script wasn't found in test-unit gem
        final String msg = "Rake runner isn't compatible with your'" + TEST_UNIT_GEM_SUFFIX + testUnitGemVersion
                           + "'(" + testUnitGemPath + ") gem. Please submit a feature request.";
        throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      }

    }

    final String[] loadPaths = sdk.getLoadPath();
    for (String path : loadPaths) {
      final String fullScriptPath = toSystemIndependentName(path + File.separatorChar + scriptPath);
      if (FileUtil.checkIfExists(fullScriptPath)) {
        return fullScriptPath;
      }
    }

    // If stderr isn't empty / JAVA_HOME error
    final RubyScriptRunner.Output gemPathsLog = sdk.getGemPathsFetchLog();
    failIfWithErrors(gemPathsLog);
    final RubyScriptRunner.Output loadPathsLog = sdk.getLoadPathsFetchLog();
    failIfWithErrors(loadPathsLog);


    // General error message
    final boolean isRuby19 = sdk.isRuby19();
    final String msg = "File '" + scriptPath
                       + "' wasn't found in Gem paths and in $LOAD_PATH of Ruby SDK with interpreter: '"
                       + sdk.getPresentableName()
                       + "'\n"
                       + (isRuby19 ? "Rake runner detected that your are using Ruby 1.9. So please install 'test-unit' gem because simplified Test::Unit framework, which is bundled in Ruby 1.9, doesn't support pluggable test reporters.\n" : "")
                       + "\n"
                       + "Gem paths:\n"
                       + gemPathsLog.getStdout()
                       + "\n"
                       + "\n"
                       + "$LOAD_PATH:\n"
                       + loadPathsLog.getStdout();
    throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
  }

  private static void failIfWithErrors(final RubyScriptRunner.Output result)
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
