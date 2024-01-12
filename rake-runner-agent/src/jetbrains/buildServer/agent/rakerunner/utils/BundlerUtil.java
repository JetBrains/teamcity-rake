

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.ModifiableRunnerContext;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.agent.rakerunner.utils.FileUtil2.*;
import static jetbrains.buildServer.rakerunner.RakeRunnerConstants.BUNDLER_GEM_VERSION_PROPERTY;
import static jetbrains.buildServer.rakerunner.RakeRunnerConstants.CUSTOM_GEMFILE_RELATIVE_PATH;

/**
 * @author Roman.Chernyatchik
 */
public class BundlerUtil {
  private static final String BUNDLER_GEM_NAME = "bundler";
  static final String BUNDLE_BIN_PATH_ENV_VAR = "BUNDLE_BIN_PATH";
  private static final String BUNDLE_GEMFILE_ENV_VAR = "BUNDLE_GEMFILE";
  private static final Pattern BUNDLE_PATH_PATTERN = Pattern.compile("^\\s*BUNDLE_PATH:\\s*(.*)$", Pattern.MULTILINE);
  private static final String BUNDLE_FOLDER = ".bundle";
  private static final String BUNDLER_FOLDER = ".bundler";
  private static final String GEM_HOME = "GEM_HOME";
  private static final String BUNDLE_PATH_ENV_VAR = "BUNDLE_PATH";

  public static boolean isBundleExecEmulationEnabled(@NotNull final Map<String, String> runParams) {
    return ConfigurationParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY);
  }


  @Nullable
  private static String getDefaultBundleGemsDir(@NotNull final RubySdk sdk) {
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
    buff.append(File.separator).append(sdk.isJRuby() ? "jruby" : "ruby");
    buff.append(File.separator).append(sdk.isRuby19() ? "1.9" : "1.8");

    final String path = buff.toString();
    if (checkIfDirExists(path)) {
      return path;
    }
    return null;
  }

  public static void enableBundleExecEmulationIfNeeded(@NotNull final RubySdk sdk,
                                                       @NotNull final ModifiableRunnerContext context)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    if (!isBundleExecEmulationEnabled(context.getRunnerParameters())) {
      return;
    }

    final String bundlerGemRootPath = findBundlerGemRoot(sdk, context.getBuildParameters());

    // BUNDLE_BIN_PATH env variable
    setBundleBinPath(context.getEnvParameters(), bundlerGemRootPath);

    // BUNDLE_GEM_FILE env variable
    final String gemFilePath = FileUtil.toSystemIndependentName(determineGemfilePath(context));
    context.getEnvParameters().put(BUNDLE_GEMFILE_ENV_VAR, gemFilePath);

    // Add BUNDLE_PATH/.../bin to PATH
    addCustomBundleGemsBinFolderToPath(sdk, context, gemFilePath);

    // RUBYLIB: Let's add bundler setup script to loadpath
    addBundlerSetupScriptToLoadPath(context.getEnvParameters(), bundlerGemRootPath);

    // RUBYOPT: attach bundler using its bundler/setup script
    attachBundler(context.getRunnerParameters(), context.getEnvParameters());
  }


  @Nullable
  public static String determineGemsRootsAccordingToBundlerSettings(@NotNull final RubySdk sdk,
                                                                    @NotNull final ModifiableRunnerContext context)
    throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    if (!isBundleExecEmulationEnabled(context.getRunnerParameters())) {
      return null;
    }

    final String customBundleFolderPath = context.getBuildParameters().get(RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH);
    if (!StringUtil.isEmpty(customBundleFolderPath)) {
      return getCustomBundlerGemsRoot(sdk, context.getCheckoutDirectory(), customBundleFolderPath);
    } else {
      // lets ignore default user-home based .bundler, seems it isn't used on run-time
      final String gemfilePath = determineGemfilePath(context);
      return getBundlerGemsDirFromConfig(sdk, gemfilePath);
    }
  }

  private static void addCustomBundleGemsBinFolderToPath(@NotNull final RubySdk sdk,
                                                         @NotNull final ModifiableRunnerContext context,
                                                         @NotNull final String gemfilePath)
    throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    String bundlerGemsRoot;
    final String customBundleFolderPath = context.getBuildParameters().get(RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH);
    final Map<String, String> env = context.getEnvParameters();
    if (!StringUtil.isEmpty(customBundleFolderPath)) {
      bundlerGemsRoot = getCustomBundlerGemsRoot(sdk, context.getCheckoutDirectory(), customBundleFolderPath);
      // just to copy behaviour when this BUNDLE_PATH is specified in config
      //env.put(GEM_HOME, bundlerGemsRoot);
      env.put(BUNDLE_PATH_ENV_VAR, bundlerGemsRoot);
    } else {
      // more correct is to determine relatively to gemfile:
      // gemfile path is already determined
      bundlerGemsRoot = getBundlerGemsDirFromConfig(sdk, gemfilePath);
      if (bundlerGemsRoot != null) {
        // bundler sets GEM_HOME according bundle_path if it is specified in config
        env.put(GEM_HOME, bundlerGemsRoot);
      } else {
        // ~/.bundle or ~/.bundler
        // don't change GEM_HOME
        bundlerGemsRoot = getDefaultBundleGemsDir(sdk);
      }
    }

    if (bundlerGemsRoot != null) {
      final String bundlerGemsBinFolder = FileUtil.toSystemDependentName(bundlerGemsRoot) + File.separator + "bin";

      OSUtil.prependToPATHEnvVariable(bundlerGemsBinFolder, env);
    }
  }

  @NotNull
  private static String getCustomBundlerGemsRoot(@NotNull final RubySdk sdk,
                                                 @Nullable final String checkoutDirPath,
                                                 @NotNull final String customBundleFolderPath)
    throws RakeTasksBuildService.MyBuildFailureException {

    String bundlerGemsRoot = checkoutDirPath != null
                             ? findGemFolderForSdk(sdk, checkoutDirPath + File.separator + customBundleFolderPath)
                             : null;
    if (bundlerGemsRoot == null) {
      // as full path:
      bundlerGemsRoot = findGemFolderForSdk(sdk, customBundleFolderPath);
      if (bundlerGemsRoot == null) {
        // cannot find:
        final String msg = "Custom bundle folder wasn't found. You set it to '" + customBundleFolderPath + "'.";
        throw new RakeTasksBuildService.MyBuildFailureException(msg);
      }
    }
    return bundlerGemsRoot;
  }

  @Nullable
  private static String getBundlerGemsDirFromConfig(@NotNull final RubySdk sdk,
                                                    @NotNull final String gemfilePath) throws RunBuildException {
    final String gemfileParentFolder = new File(gemfilePath).getParent();
    final String configPath = gemfileParentFolder + File.separator + ".bundle" + File.separator + "config";

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
      String bundlePath = gemfileParentFolder + File.separator + dir;
      if (!checkIfDirExists(bundlePath)) {
        // else if local path
        if (dir.startsWith("~")) {
          bundlePath = OSUtil.getUserHomeFolder() + dir.substring(1);
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
  public static String determineGemfilePath(@NotNull final ModifiableRunnerContext context) throws RakeTasksBuildService.MyBuildFailureException {
    final Map<String, String> runnerEnvParams = context.getEnvParameters();
    final Map<String, String> buildParams = context.getBuildParameters();
    final String userDefinedGemFilePath = runnerEnvParams.get(BUNDLE_GEMFILE_ENV_VAR);
    if (!StringUtil.isEmpty(userDefinedGemFilePath)) {
      return userDefinedGemFilePath;
    }

    final boolean oldResolve = ConfigurationParamsUtil.isParameterEnabled(buildParams, RakeRunnerConstants.GEMFILE_RESOLVE_IN_CHECKOUT_DIRECTORY);
    final String resolvingBaseDirectory = oldResolve ? context.getCheckoutDirectory() : context.getWorkingDirectory();

    final String customGemFilePath = buildParams.get(CUSTOM_GEMFILE_RELATIVE_PATH);
    String gemfilePath;
    if (!StringUtil.isEmpty(customGemFilePath)) {
      if (FileUtil.isAbsolute(customGemFilePath)) {
        return customGemFilePath;
      }
      // use custom
      gemfilePath = resolvingBaseDirectory + File.separator + customGemFilePath;
      if (checkIfExists(gemfilePath)) {
        return gemfilePath;
      }
      final String msg = "Gemfile wasn't found. You specified path to file '" + gemfilePath + "'.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    } else {
      // default one
      final String[] gemFileNames = new String[]{"Gemfile", "GemFile", "gemfile"};
      for (String name : gemFileNames) {
        gemfilePath = resolvingBaseDirectory + File.separator + name;
        if (checkIfExists(gemfilePath)) {
          return gemfilePath;
        }
      }
      final String pattern = "Cannot find Gemfile in %s directory : '%s'. \n" +
                             "If Gemfile is located in other directory please specify Gemfile relative path using system property: " + CUSTOM_GEMFILE_RELATIVE_PATH;
      String msg;
      if (oldResolve) {
        msg = String.format(pattern, "checkout", context.getCheckoutDirectory());
      } else {
        msg = String.format(pattern, "working", context.getWorkingDirectory());
      }
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    }
  }

  private static void attachBundler(@NotNull final Map<String, String> runParams,
                                    @NotNull final Map<String, String> runnerEnvParams) {
    // RUBYOPT: attach bundler using its bundler/setup script
    String bundlerExecCommand = "-rbundler/setup";

    // if project uses with test-unit gem and uses bundler
    // if Test::Unit bundled in sdk is used such patch will not break anything, so let's simplify check:
    if (SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams)) {
      // for compatibility with : test-unit gem + bundler (see issue [RUBY-6192] - http://youtrack.jetbrains.net/issue/RUBY-6192)
      // we need to re-apply our load patch hack

      // our test::unit loadpath is already loaded, but isn't first in $LOAD_PATH
      // thus we can require our file by relative path.
      bundlerExecCommand += " -r" + RubyProjectSourcesUtil.TUNIT_LOADPATH_PATH_SCRIPT;
    }
    OSUtil.prependToRUBYOPTEnvVariable(bundlerExecCommand, runnerEnvParams);
  }

  private static void addBundlerSetupScriptToLoadPath(@NotNull final Map<String, String> runnerEnvParams,
                                                      @NotNull final String bundlerGemRootPath)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {
    // RUBYLIB: Let's add bundler setup script to loadpath
    // it's better than -I option in RUBYOPT because path may contain whitespaces
    File libFolder = null;
    for (String path: Arrays.asList("lib", "libexec", "exe", "lib")) { // the last "lib" element is needed to save the old logging
      libFolder = new File(bundlerGemRootPath + File.separator + path);
      if (libFolder.exists()) break;
    }

    checkIfFolderExist(libFolder);

    OSUtil.appendToRUBYLIBEnvVariable(getCanonicalPath(libFolder), runnerEnvParams);
  }

  static void setBundleBinPath(@NotNull final Map<String, String> runnerEnvParams,
                                      @NotNull final String bundlerGemRootPath)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {
    if (StringUtil.isEmpty(runnerEnvParams.get(BUNDLE_BIN_PATH_ENV_VAR))) {
      // if user doesn't overload bundle bin path

      // Modern bundler:
      // Starting from 1.11.0 is
      // BUNDLE_BIN_PATH=/Users/vlad/.rbenv/versions/2.4.2/lib/ruby/gems/2.4.0/gems/bundler-1.12.6/exe/bundle
      // In 1.0.0 and newer
      // BUNDLE_BIN_PATH=/Users/vlad/.rbenv/versions/2.4.2/lib/ruby/gems/2.4.0/gems/bundler-1.7.15/bin/bundle
      // In 0.9 without 'bundle' ???

      final File bundle = FileUtil2.getFirstExistChild(new File(bundlerGemRootPath), "exe/bundle", "bin/bundle");
      if (bundle == null) {
        final String msg = "Unsupported bundler gem version: Cannot find 'bundle' executable under '" + bundlerGemRootPath + "'.";
        throw new RakeTasksBuildService.MyBuildFailureException(msg);
      }
      runnerEnvParams.put(BUNDLE_BIN_PATH_ENV_VAR, getCanonicalPath(bundle));
    }
  }

  private static void checkIfFolderExist(@NotNull final File folder) throws RakeTasksBuildService.MyBuildFailureException {
    if (!folder.exists()) {
      final String msg = "Unsupported bundler gem version: Cannot find '" + folder.getPath() + "'.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    }
  }

  /**
   * Finds bundler gem installation directory in gem paths
   *
   * @param buildParameters Build params
   * @return Directory full path
   * @throws jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService.MyBuildFailureException If gem root wasn't found
   */
  @NotNull
  private static String findBundlerGemRoot(@NotNull final RubySdk sdk,
                                           @NotNull final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException {

    // At first let's try to find script in "test-unit" gem
    // then in sdk load path

    final String forcedBundlerGemVersion = RubySDKUtil.getForcedGemVersion(BUNDLER_GEM_VERSION_PROPERTY, buildParameters);

    // P.S: we are not interested to search bundler gem in bundler git paths or in "frozen" bundler paths
    final Pair<String, String> gemInfo = RubySDKUtil.findGemRootFolderAndVersion(BUNDLER_GEM_NAME,
                                                                                 sdk.getGemPaths(),
                                                                                 forcedBundlerGemVersion);


    if (gemInfo != null) {
      final String bundlerGemPath = gemInfo.first;
      if (checkIfExists(bundlerGemPath)) {
        return bundlerGemPath;
      } else {
        // Error: bundler gem home directory wasn't found
        final String msg = "Expected bundler gem installation directory doesn't exist: '" + bundlerGemPath + "'.";
        throw new RakeTasksBuildService.MyBuildFailureException(msg);
      }
    } else {
      // bundler gem not found
      if (forcedBundlerGemVersion != null) {
        // forced version
        final StringBuilder msg = new StringBuilder();
        msg.append("bundler gem with version '").append(forcedBundlerGemVersion)
          .append("' wasn't found in Gem paths of Ruby SDK with interpreter: '").append(sdk.getName()).append("'.\n").append("Gem paths:\n")
          .append(sdk.getGemPathsFetchLog().getStdout());
        throw new RakeTasksBuildService.MyBuildFailureException(msg.toString());
      } else {
        // any version
        final StringBuilder msg = new StringBuilder();
        msg.append(
          "If you want to use bundler please install it at first. The gem wasn't found in Gem paths of Ruby SDK with interpreter: '");
        msg.append(sdk.getName()).append("'.\n");
        msg.append("Gem paths:\n").append(sdk.getGemPathsFetchLog().getStdout());
        throw new RakeTasksBuildService.MyBuildFailureException(msg.toString());
      }
    }
  }
}