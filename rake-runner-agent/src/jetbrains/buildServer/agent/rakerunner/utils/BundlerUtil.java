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
import com.intellij.openapi.util.io.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.agent.rakerunner.utils.FileUtil.*;
import static jetbrains.buildServer.rakerunner.RakeRunnerConstants.BUNDLER_GEM_VERSION_PROPERTY;
import static jetbrains.buildServer.rakerunner.RakeRunnerConstants.CUSTOM_GEMFILE_RELATIVE_PATH;

/**
 * @author Roman.Chernyatchik
 */
public class BundlerUtil {
  private static final String BUNDLER_GEM_NAME = "bundler";
  private static final String BUNDLE_BIN_PATH_ENV_VAR = "BUNDLE_BIN_PATH";
  public static final String BUNDLE_GEMFILE_ENV_VAR = "BUNDLE_GEMFILE";
  private static final Pattern BUNDLE_PATH_PATTERN = Pattern.compile("^\\s*BUNDLE_PATH:\\s*(.*)$", Pattern.MULTILINE);
  private static final String BUNDLE_FOLDER = ".bundle";
  private static final String BUNDLER_FOLDER = ".bundler";
  private static final String GEM_HOME = "GEM_HOME";
  private static final String BUNDLE_PATH_ENV_VAR = "BUNDLE_PATH";

  public static boolean isBundleExecEmulationEnabled(final Map<String, String> runParams) {
    return ConfigurationParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY);
  }


  @Nullable
  private static String getDefaultBundleGemsDir(final RubySdk sdk) {
    final String userHomeFolder = OSUtil.getUserHomeFolder();

    final String bundlePath = findGemFolderForSdk(sdk, userHomeFolder + File.separator + BUNDLER_FOLDER);
    if (bundlePath != null) {
      return bundlePath;
    }
    return findGemFolderForSdk(sdk, userHomeFolder + File.separator + BUNDLE_FOLDER);
  }

  @Nullable
  private static String findGemFolderForSdk(@NotNull final RubySdk sdk,
                                            @NotNull final String bundlePath) {
    final StringBuilder buff = new StringBuilder(bundlePath);
    final boolean isJRuby = sdk.isJRuby();
    buff.append(File.separator).append(isJRuby ? "jruby" : "ruby");
    final boolean is19 = sdk.isRuby19();
    buff.append(File.separator).append(is19 ? "1.9" : "1.8");

    final String path = buff.toString();
    if (checkIfDirExists(path)) {
      return path;
    }
    return null;
  }

  public static void enableBundleExecEmulationIfNeeded(@NotNull final RubySdk sdk,
                                                       @NotNull final Map<String, String> runParams,
                                                       @NotNull final Map<String, String> buildParams,
                                                       @NotNull final Map<String, String> runnerEnvParams,
                                                       @NotNull final String checkoutDirPath)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    if (!isBundleExecEmulationEnabled(runParams)) {
      return;
    }

    final String bundlerGemRootPath = findBundlerGemRoot(sdk, buildParams);

    // BUNDLE_BIN_PATH env variable
    setBundleBinPath(runnerEnvParams, bundlerGemRootPath);

    // BUNDLE_GEM_FILE env variable
    final String gemFilePath = determineGemfilePath(buildParams, runnerEnvParams, checkoutDirPath);
    runnerEnvParams.put(BUNDLE_GEMFILE_ENV_VAR, gemFilePath);

    // Add BUNDLE_PATH/.../bin to PATH
    addCustomBundleGemsBinFolderToPath(sdk, buildParams, runnerEnvParams, checkoutDirPath, gemFilePath);

    // RUBYLIB: Let's add bundler setup script to loadpath
    addBundlerSetupScriptToLoadPath(runnerEnvParams, bundlerGemRootPath);

    // RUBYOPT: attach bundler using its bundler/setup script
    attachBundler(runParams, runnerEnvParams);
  }


  @Nullable
  public static String determineGemsRootsAccordingToBundlerSettings(final RubySdk sdk,
                                                                    @NotNull final Map<String, String> runParams,
                                                                    final Map<String, String> buildParams,
                                                                    final Map<String, String> runnerEnvParams,
                                                                    final String checkoutDirPath)
    throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    if (!isBundleExecEmulationEnabled(runParams)) {
      return null;
    }

    final String customBundleFolderPath = buildParams.get(RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH);
    if (!StringUtil.isEmpty(customBundleFolderPath)) {
      return getCustomBundlerGemsRoot(sdk, checkoutDirPath, customBundleFolderPath);
    } else {
      // lets ignore default user-home based .bundler, seems it isn't used on run-time
      final String gemfilePath = determineGemfilePath(buildParams, runnerEnvParams, checkoutDirPath);
      return getBundlerGemsDirFromConfig(sdk, gemfilePath);
    }
  }

  private static void addCustomBundleGemsBinFolderToPath(final RubySdk sdk,
                                                         final Map<String, String> buildParams,
                                                         final Map<String, String> runnerEnvParams,
                                                         final String checkoutDirPath,
                                                         final String gemfilePath)
    throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    String bundlerGemsRoot = null;
    final String customBundleFolderPath = buildParams.get(RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH);
    if (!StringUtil.isEmpty(customBundleFolderPath)) {
      bundlerGemsRoot = getCustomBundlerGemsRoot(sdk, checkoutDirPath, customBundleFolderPath);
      // just to copy behaviour when this BUNDLE_PATH is specified inconfig
      //runnerEnvParams.put(GEM_HOME, bundlerGemsRoot);
      runnerEnvParams.put(BUNDLE_PATH_ENV_VAR, bundlerGemsRoot);
    } else {
      // more correct is to determine relatively to gemfile:
      // gemfile path is already determined
      bundlerGemsRoot = getBundlerGemsDirFromConfig(sdk, gemfilePath);
      if (bundlerGemsRoot != null) {
        // bundler sets GEM_HOME according bundle_path if it is specified in config
        runnerEnvParams.put(GEM_HOME, bundlerGemsRoot);
      } else {
        // ~/.bundle or ~/.bundler
        // don't change GEM_HOME
        bundlerGemsRoot = getDefaultBundleGemsDir(sdk);
      }
    }

    if (bundlerGemsRoot != null) {
      final String bundlerGemsBinFolder = FileUtil.toSystemDependentName(bundlerGemsRoot) + File.separator + "bin";

      OSUtil.prependToPATHEnvVariable(bundlerGemsBinFolder, runnerEnvParams);
    }
  }

  private static String getCustomBundlerGemsRoot(final RubySdk sdk, final String checkoutDirPath, final String customBundleFolderPath)
    throws RakeTasksBuildService.MyBuildFailureException {

    String bundlerGemsRoot = findGemFolderForSdk(sdk, checkoutDirPath + File.separator + customBundleFolderPath);
    if (bundlerGemsRoot == null) {
      // as full path:
      bundlerGemsRoot = findGemFolderForSdk(sdk, customBundleFolderPath);
      if (bundlerGemsRoot == null) {
        // cannot find:
        final String msg = "Custom bundle folder wasn't found. You set it to '" + customBundleFolderPath + "'.";
        throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      }
    }
    return bundlerGemsRoot;
  }

  private static String getBundlerGemsDirFromConfig(final RubySdk sdk,
                                                    final String gemfilePath) throws RunBuildException {
    final String gemfileParentFoler = gemfilePath.substring(0, gemfilePath.lastIndexOf("/"));
    final String configPath = gemfileParentFoler + File.separator + ".bundle" + File.separator + "config";

    // file separators aren't important here
    if (!checkIfExists(configPath)) {
      return null;
    }

    final String text;
    try {
      text = new String(FileUtil.loadFileText(new File(configPath)));
    } catch (IOException e) {
      throw new RunBuildException(e);
    }
    final Matcher matcher = BUNDLE_PATH_PATTERN.matcher(text);
    if (matcher.find()) {
      final String dir = matcher.group(1);
      // if relative path:
      String bundlePath = gemfileParentFoler + File.separator + dir;
      if (!checkIfDirExists(bundlePath)) {
        // else if local path
        if (dir.startsWith("~")) {
          bundlePath = dir.replaceFirst("~", OSUtil.getUserHomeFolder());
        }
        if (!checkIfDirExists(bundlePath)) {
          // else if full path
          bundlePath = dir;
        }
      }
      if (checkIfDirExists(bundlePath)) {
        return findGemFolderForSdk(sdk, bundlePath);
      }
    }
    return null;
  }

  @NotNull
  private static String determineGemfilePath(@NotNull final Map<String, String> buildParams,
                                             @NotNull final Map<String, String> runnerEnvParams,
                                             @NotNull final String checkoutDirPath) throws RakeTasksBuildService.MyBuildFailureException {

    final String userDefinedGemFilePath = runnerEnvParams.get(BUNDLE_GEMFILE_ENV_VAR);
    if (!StringUtil.isEmpty(userDefinedGemFilePath)) {
      return userDefinedGemFilePath;
    }

    final String customGemFileRelativePath = buildParams.get(CUSTOM_GEMFILE_RELATIVE_PATH);
    if (!StringUtil.isEmpty(customGemFileRelativePath)) {
      // use custom runner
      final String gemfilePath = checkoutDirPath + File.separator + customGemFileRelativePath;
      if (checkIfExists(gemfilePath)) {
        return gemfilePath;
      }
      final String msg = "Gemfile wasn't found. You specified path to file '"+ gemfilePath + "'.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    } else {
      // default one
      final String[] gemFileNames = new String[] {"Gemfile", "GemFile", "gemfile"};
      for (String name : gemFileNames) {
        final String gemfilePath = checkoutDirPath + File.separator + name;
        if (checkIfExists(gemfilePath)) {
          return gemfilePath;
        }
      }
      final String msg = "Cannot find Gemfile in checkout directory : '"+ checkoutDirPath
                         + "'. If Gemfile is located in other directory please specify Gemfile relative path using system propery: " + CUSTOM_GEMFILE_RELATIVE_PATH;
      throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }
  }

  private static void attachBundler(final Map<String, String> runParams, final Map<String, String> runnerEnvParams) {
    // RUBYOPT: attach bundler using its bundler/setup script
    String bundlerExecCommand = "-rbundler/setup";

    // if project uses with test-unit gem and uses bundler
    // if Test::Unit bundled in sdk is used such patch will not break anything, so let's simlify check:
    if (SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams)) {
      // for compatibility with : test-unit gem + bundler (see issue [RUBY-6192] - http://youtrack.jetbrains.net/issue/RUBY-6192)
      // we need to re-apply our load patch hack

      // our test::unit loadpath is already loaded, but isn't first in $LOAD_PATH
      // thus we can require our file by relative path.
      bundlerExecCommand += " -r" + RubyProjectSourcesUtil.TUNIT_LOADPATH_PATH_SCRIPT;
    }
    OSUtil.prependToRUBYOPTEnvVariable(bundlerExecCommand, runnerEnvParams);
  }

  private static void addBundlerSetupScriptToLoadPath(final Map<String, String> runnerEnvParams, final String bundlerGemRootPath)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {
    // RUBYLIB: Let's add bundler setup script to loadpath
    // it's better than -I option in RUBYOPT because path may contain whitespaces
    final File libFolder = new File(bundlerGemRootPath + File.separator + "lib");
    checkIfFolderExist(libFolder);

    OSUtil.appendToRUBYLIBEnvVariable(getCanonicalPath(libFolder), runnerEnvParams);
  }

  private static void setBundleBinPath(final Map<String, String> runnerEnvParams, final String bundlerGemRootPath)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {
    if (StringUtil.isEmpty(runnerEnvParams.get(BUNDLE_BIN_PATH_ENV_VAR))) {
      // if user doesn't overload bundle bin path

      final File binFolder = new File(bundlerGemRootPath + File.separator + "bin");
      checkIfFolderExist(binFolder);
      runnerEnvParams.put(BUNDLE_BIN_PATH_ENV_VAR, getCanonicalPath(binFolder));
    }
  }

  private static void checkIfFolderExist(final File folder) throws RakeTasksBuildService.MyBuildFailureException {
    if (!folder.exists()) {
      final String msg = "Unsupported bundler gem version: Cannot find '" + folder.getPath() + "'.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }
  }

  /**
   * Finds bundler gem installation directory in gem paths
   * @param buildParameters Build params
   * @return Direcory full path
   *
   * @throws jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService.MyBuildFailureException If gem root wasn't found
   */
  @NotNull
  private static String findBundlerGemRoot(@NotNull final RubySdk sdk,
                                           final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException {

    // At first let's try to find script in "test-unit" gem
    // then in sdk load path

    final String forcedBundlerGemVersion = RubySDKUtil.getForcedGemVersion(BUNDLER_GEM_VERSION_PROPERTY, buildParameters);

    // P.S: we are not intereseted to search bundler gem in bundler git paths or in "frozen" bundler paths
    final Pair<String, String> pathAndVersion = RubySDKUtil.findGemRootFolderAndVersion(BUNDLER_GEM_NAME,
                                                                                        sdk.getGemPaths(),
                                                                                        forcedBundlerGemVersion);
    final String bundlerGemPath = pathAndVersion.first;


    if (bundlerGemPath != null) {
      if (checkIfExists(bundlerGemPath)) {
        return bundlerGemPath;
      } else {
        // Error: bundler gem home directory wasn't found
        final String msg = "Expected bundler gem installation directory doesn't exist: '"+ bundlerGemPath + "'.";
        throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      }
    } else {
      // bundler gem not found
      if (forcedBundlerGemVersion != null) {
        // forced version
        final String msg = "bundler gem with version '"
                           + forcedBundlerGemVersion
                           + "' wasn't found in Gem paths of Ruby SDK with interpreter: '"
                           + sdk.getPresentableName()
                           + "'.\n"
                           + "Gem paths:\n"
                           + sdk.getGemPathsFetchLog().getStdout();
        throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      } else {
        // any version
        final String msg = "If you wan't to use bundler please install it at first. The gem wasn't found in Gem paths of Ruby SDK with interpreter: '"
                           + sdk.getPresentableName()
                           + "'.\n"
                           + "Gem paths:\n"
                           + sdk.getGemPathsFetchLog().getStdout();
        throw new RakeTasksBuildService.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      }
    }
  }
}
