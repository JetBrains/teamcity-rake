package org.jetbrains.plugins.ruby.rvm;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Roman.Chernyatchik
 *
 * Shared with TeamCity
 */
class SharedRVMUtil {
  public interface Constants {
    String LOCAL_RVM_HOME_FOLDER_NAME = ".rvm";
    String RVM_GEMS_FOLDER_NAME = "gems";
    String RVM_RUBIES_FOLDER_NAME = "rubies";
    String RVM_BIN_FOLDER_RELATIVE_PATH = "/bin";
    String DEFAULT_GEMSET_SEPARATOR = "@";
    String RVM_GEMSET_SEPARATOR_ENVVAR = "rvm_gemset_separator";
    String GLOBAL_GEMSET_NAME = "global";
    String GEM_PATH = "GEM_PATH";
    String GEM_HOME = "GEM_HOME";
    String BUNDLE_PATH = "BUNDLE_PATH";
    String MY_RUBY_HOME = "MY_RUBY_HOME";
    String GEMS_ROOT_RELATIVE_PATH = "/gems";
    String RVM_RUBY_STRING = "rvm_ruby_string";
    String RVM_GEMSET = "gemset";

    String[] SYSTEM_RVM_ENVVARS_TO_RESET = new String[] {
      SharedRVMUtil.Constants.GEM_HOME,
      SharedRVMUtil.Constants.GEM_PATH,
      SharedRVMUtil.Constants.BUNDLE_PATH,
      SharedRVMUtil.Constants.MY_RUBY_HOME,
      SharedRVMUtil.Constants.RVM_GEMSET
    };
  }

  private SharedRVMUtil() {
  }

  @Nullable
  public static String determineSuitableRVMSdkDist(@NotNull final String rvmrcSdkRef,
                                                   @Nullable final String rvmrcGemset,
                                                   @NotNull final RubyDistToGemsetTable distName2GemsetsTable) {
    for (Map.Entry<String, List<String>> distAndGemsets : distName2GemsetsTable.myRubyDist2Gemset.entrySet()) {
      final String distName = distAndGemsets.getKey();

      // check either refName suites dist name or not
      if (sdkRefMatches(rvmrcSdkRef, distName)) {

        // check gemsets
        final List<String> gemsets = distAndGemsets.getValue();
        for (String gemset : gemsets) {
          if (areGemsetsEqual(rvmrcGemset, gemset)) {
            return distName;
          }
        }
      }
    }
    return null;
  }

  public static boolean sdkRefMatches(@NotNull final String sdkRef,
                                      @NotNull final String distName) {
    // starts with
    // e.g. [ruby-1.8.7] and [ruby-1.8.7-p249, ruby-1.8.7, ruby-1.8.7-2010.01]
    // TODO [romeo]: rvm use jruby will chose jruby with highest version!
    if (distName.startsWith(sdkRef)) {
      return true;
    }

    // if ref is just version:
    // jruby-1.[3-6]
    if (sdkRef.startsWith("1.3") || sdkRef.startsWith("1.4") || sdkRef.startsWith("1.5") || sdkRef.startsWith("1.6")) {
      // try with "jruby-" prefixes
      return distName.startsWith("jruby-" + sdkRef);
    }
    // ruby-1.[8-9]
    if (sdkRef.startsWith("1.8") || sdkRef.startsWith("1.9") || sdkRef.startsWith("2.0")) {
      // try with "ruby-" prefixes
      return distName.startsWith("ruby-" + sdkRef);
    }

    return false;
  }

  public static boolean isRVMInterpreter(@NotNull final String executablePath) {
    if (!SystemInfo.isUnix) {
      return false;
    }
    final String rvmHomePath = RVMPathsSettings.getInstance().getRvmHomePath();
    if (rvmHomePath == null) {
      return false;
    }
    return executablePath.startsWith(rvmHomePath + "/" + Constants.RVM_RUBIES_FOLDER_NAME);
  }

  public static String getGemsetSeparator() {
    if (SystemInfo.isUnix) {
      // RVM supported on Unix systems

      // 1. get from env variable
      // (e.g. Linux or good Mac)
      final String separator = System.getenv(Constants.RVM_GEMSET_SEPARATOR_ENVVAR);
      if (separator != null) {
        return separator;
      }
    }
    return Constants.DEFAULT_GEMSET_SEPARATOR;
  }

  public static void registerGemset(@NotNull final String rvmGemsSubFolder,
                             @NotNull final Condition<String> isRVMDistCondition,
                             @NotNull final RubyDistToGemsetTable rubyDist2Gemset) {
    final int separatorIndex = rvmGemsSubFolder.indexOf(getGemsetSeparator());

    // 1. [dist][@][gemset] or just [dist] with default gemset
    final String distName = separatorIndex == -1 ? rvmGemsSubFolder : rvmGemsSubFolder.substring(0, separatorIndex);
    final String gemset = separatorIndex == -1 ? null : rvmGemsSubFolder.substring(separatorIndex + 1);

    // 2. after invoking rvm command with illegal arguments rvm creates tons of garbage
    // for such not existing sdks and gemsets
    // e.g. "ruby-1.9.1@projecta/" instead of "/ruby-1.9.1-p378@projecta/"
    // such rake ruby sdks will not be registered in ~/.rvm/rubies
    //
    // so let's do initial spam-check:
    if (!isRVMDistCondition.value(distName)) {
      // garbage detected!
      return;
    }

    // 3. add gem to table. We won't ignore "global" gempath just to allow user install gems in
    // "shared" gem path from RubyMine
    rubyDist2Gemset.putGemset(gemset != null && gemset.length() > 0 ? gemset : null, distName);
  }

  public static boolean areGemsetsEqual(@Nullable final String gemset1,
                                        @Nullable final String gemset2) {
    return (gemset1 == null && gemset2 == null)               // if gemset is default (null)
           || (gemset1 != null && gemset1.equals(gemset2));      // or custom
  }

  @Nullable
  public static Pair<String, String> getMainAndGlobalGemPaths(@NotNull final String executablePath,
                                                              @Nullable final String gemSetName) throws IllegalArgumentException {
    final String baseGemsPath = getBaseGemsPath(executablePath);
    if (baseGemsPath == null) {
      return null;
    }

    final String gs = getGemsetSeparator();
    final boolean useDefaultGemset = gemSetName == null;

    final String mainGemPath = useDefaultGemset ? baseGemsPath : baseGemsPath + gs + gemSetName;
    final String globalGSGemPath = baseGemsPath + gs + Constants.GLOBAL_GEMSET_NAME;
    return new Pair<String, String>(mainGemPath, globalGSGemPath);
  }

  /**
   *
   *
   * @param executablePath
   * @param gemSetName
   * @param isSystemRvm
   * @param gemsRootsPaths
   * @param envParams
   * @param userDefinedEnvVars
   * @param globalGempathIgnored
   * @param pathSeparator
   * @param pathEnvVarName
   * @param defaultEnvVars If specified then userDefinedEnvVars with default values can be overridden        @throws IllegalArgumentException
   */
  public static void patchEnvForRVM(@NotNull final String executablePath,
                                    @Nullable final String gemSetName,
                                    final boolean isSystemRvm,
                                    @NotNull final Collection<String> gemsRootsPaths,
                                    @NotNull final Map<String, String> envParams,
                                    @NotNull final Map<String, String> userDefinedEnvVars,
                                    final boolean globalGempathIgnored,
                                    final char pathSeparator,
                                    final String pathEnvVarName,
                                    @Nullable final Map<String, String> defaultEnvVars) throws IllegalArgumentException {

    // Supports:
    //   PATH,
    //   GEM_HOME,
    //   GEM_PATH,
    //   BUNDLE_PATH - gem path for bundler gem integration
    //   MY_RUBY_HOME  - rvm ruby sdk dist folder location
    //   rvm_ruby_string - rvm ruby sdk dist name
    //
    // rvm overrides GEM_PATH, GEM_HOME event if it exists!
    //
    // sdk_in_current_gems_set:
    //  = [sdk_name]
    //  for default gems set
    //  or
    //  = [sdk_name]%[gem_set_name]
    // for [gem_set_name] gem set
    //
    // GEM_HOME = ~/.rvm/gems/[sdk_in_current_gems_set]
    // GEM_PATH = ~/.rvm/gems/[sdk_in_current_gems_set]:~/.rvm/gems/[sdk_name]%global
    //
    // p.s: % separator may be replaced with custom separator, even :-) or +. See
    // rvm_gemset_separator property of .rvmrc file
    // (see RUBY-5848)
    //
    // 3 locations of rvmrc files:
    // * /etc/rvmrc
    // * ~/.rvmrc
    // * [project]/.rvmrc
    //

    if (isSystemRvm) {
      // We need to reset variables:
      //    GEM_HOME:     ""
      //    GEM_PATH:     ""
      //    BUNDLE_PATH:  ""
      //    MY_RUBY_HOME: ""
      //    IRBRC:        ""
      //    gemset:       ""
      for (String envVarName : Constants.SYSTEM_RVM_ENVVARS_TO_RESET) {
        if (canOverride(envVarName, userDefinedEnvVars, defaultEnvVars)) {
          envParams.remove(envVarName);
        }
      }
      return;
    }

    if (gemsRootsPaths.size() > 2) {
      throw new IllegalArgumentException("Not more than 2 gems roots are expected here, but was:\n" + gemsRootsPaths.toString());
    }

    // rvm_ruby_string
    final Pair<String, String> rootAndDist = getRVMGemsRootAndDistName(executablePath);
    final String unsupportedRvmVersionError = "Probably you are using unsupported RVM gem version. Ruby interpreter: " + executablePath;
    if (rootAndDist == null || rootAndDist.second == null) {
      throw new IllegalArgumentException(unsupportedRvmVersionError);
    }
    envParams.put(Constants.RVM_RUBY_STRING, rootAndDist.second);

    final Pair<String, String> mainAndGlobalGemPaths = getMainAndGlobalGemPaths(executablePath, gemSetName);
    if (mainAndGlobalGemPaths == null) {
      throw new IllegalArgumentException(unsupportedRvmVersionError);
    }

    final String mainGemPath = mainAndGlobalGemPaths.first;

    // gemset
    if (canOverride(Constants.RVM_GEMSET, userDefinedEnvVars, defaultEnvVars)) {
      envParams.put(Constants.RVM_GEMSET, gemSetName != null ? gemSetName : "");
    }

    // GEM_HOME = ~/.rvm/gems/[sdk_in_current_gems_set]
    if (canOverride(Constants.GEM_HOME, userDefinedEnvVars, defaultEnvVars)) {
      envParams.put(Constants.GEM_HOME, mainGemPath);
    }

    // BUNDLE_PATH - seems to be same as GEM_HOME
    // [!!!] - change with bundler path method located in BundlerUtil
    if (canOverride(Constants.BUNDLE_PATH, userDefinedEnvVars, defaultEnvVars)) {
      envParams.put(Constants.BUNDLE_PATH, mainGemPath);
    }

    // GEM_PATH = ~/.rvm/gems/[sdk_in_current_gems_set]:~/.rvm/gems/[sdk_name]%global
    final boolean isGlobalGS = Constants.GLOBAL_GEMSET_NAME.equals(gemSetName);
    if (canOverride(Constants.GEM_PATH, userDefinedEnvVars, defaultEnvVars)) {
      // for consistency let's get urls from GemManager
      final StringBuilder gemPaths = new StringBuilder();
      for (String sdkGemsRootPath : gemsRootsPaths) {
        if (gemPaths.length() != 0) {
          gemPaths.append(pathSeparator);
        }
        if (!sdkGemsRootPath.endsWith(Constants.GEMS_ROOT_RELATIVE_PATH)) {
          throw new IllegalArgumentException("Incorrect gems root: " + sdkGemsRootPath);
        }
        final String gemPath = sdkGemsRootPath.substring(0, sdkGemsRootPath.length() - Constants.GEMS_ROOT_RELATIVE_PATH.length());
        gemPaths.append(gemPath);
      }
      envParams.put(Constants.GEM_PATH, gemPaths.toString());
    }

    // [SDK bin folder]
    final String sdkBinFolder = getParentDir(executablePath);
    if (sdkBinFolder == null) {
      throw new IllegalArgumentException("Sdk bin folder can't be null, interpreter path: " + executablePath);
    }

    //MY_RUBY_HOME - sdk home
    if (canOverride(Constants.MY_RUBY_HOME, userDefinedEnvVars, defaultEnvVars)) {
      final String sdkDistFolder = getParentDir(sdkBinFolder);
      if (sdkDistFolder == null) {
        throw new IllegalArgumentException("Sdk distributive folder can't be null, interpreter path: " + sdkDistFolder);
      }

      envParams.put(Constants.MY_RUBY_HOME, sdkDistFolder);
    }

    // Also rvm patches PATH env variable:
    // PATH = [SDK bin folder]:~/.rvm/gems/[sdk_in_current_gems_set]/bin:~/.rvm/gems/[sdk_name]%global/bin:~/.rvm/bin:$PATH
    // E.g.
    // PATH = ~/.rvm/rubies/jruby-1.4.0/bin:~/.rvm/gems/jruby-1.4.0/bin:~/.rvm/gems/jruby-1.4.0%global/bin:~/.rvm/bin:$PATH
    if (canOverride(pathEnvVarName, userDefinedEnvVars, defaultEnvVars)) {
      final StringBuilder patchedPath = new StringBuilder();

      patchedPath.append(sdkBinFolder);

      // :~/.rvm/gems/[sdk_in_current_gems_set]/bin
      patchedPath.append(pathSeparator).append(mainGemPath).append("/bin");

      // :/.rvm/gems/[sdk_name]%global/bin
      if (!isGlobalGS && !globalGempathIgnored) {
        final String globalGSGemPath = mainAndGlobalGemPaths.second;
        patchedPath.append(pathSeparator).append(globalGSGemPath).append("/bin");
      }

      // :~/.rvm/bin
      final String rvmHome = executablePath.substring(0, executablePath.indexOf("/" + Constants.RVM_RUBIES_FOLDER_NAME + "/"));
      patchedPath.append(pathSeparator).append(rvmHome).append(Constants.RVM_BIN_FOLDER_RELATIVE_PATH);

      // add old $PATH
      final String currentPath = envParams.get(pathEnvVarName);
      if (currentPath != null) {
        patchedPath.append(pathSeparator).append(currentPath);
      }
      envParams.put(pathEnvVarName, patchedPath.toString());
    }
  }

  public static boolean canOverride(@NotNull final String envVariable,
                                    @NotNull final Map<String, String> userDefinedEnvVars,
                                    @Nullable final Map<String, String> defaultEnv) {
    if (!userDefinedEnvVars.containsKey(envVariable)) {
      return true;
    }

    if (defaultEnv != null) {
      // if not null than allowed to be overridden

      // check whether useDefinedEnvs contains default value or not.
      final String userValue = userDefinedEnvVars.get(envVariable);
      final String defaultValue = defaultEnv.get(envVariable);
      if (userValue == null) {
        // both need to be null
        return defaultValue == null;
      } else {
        // both need to be equal
        return userValue.equals(defaultValue);
      }
    }
    return false;
  }

  @Nullable
  private static String getParentDir(@Nullable final String urlOrPath) {
    if (urlOrPath == null) {
      return null;
    }
    // is used for VirtualFiles urls and java.io.File names
    final int index = Math.max(urlOrPath.lastIndexOf("/"), urlOrPath.lastIndexOf("\\"));
    return index < 0 ? null : urlOrPath.substring(0, index);
  }

  @Nullable
  public static String getBaseGemsPath(@NotNull final String executablePath) throws IllegalArgumentException {
    final Pair<String, String> rootAndDist = getRVMGemsRootAndDistName(executablePath);
    if (rootAndDist == null) {
      return null;
    }
    // base gems path folder
    return rootAndDist.first + "/" + rootAndDist.second;
  }

  public static LinkedHashSet<String> determineGemRootsPaths(@NotNull final String rvmInterpreterPath,
                                                             @Nullable final String gemset,
                                                             final boolean gempathIgnored) {
    final Pair<String, String> paths;
    final LinkedHashSet<String> result = new LinkedHashSet<String>();

    paths = getMainAndGlobalGemPaths(rvmInterpreterPath, gemset);
    assert paths != null;

    // main path
    result.add(paths.first + Constants.GEMS_ROOT_RELATIVE_PATH);

    //TODO: Seems issue "[RUBY-5879] Git gems in Gemfile not recognized " is related to some old configuration
    //BundlerUtil.addRVMBundlePath(result, paths.first);

    // global path is optional
    if (!gempathIgnored) {
      result.add(paths.second + Constants.GEMS_ROOT_RELATIVE_PATH);
    }

    return result;
  }

  @Nullable
  public static Pair<String, String> getRVMGemsRootAndDistName(@NotNull final String executablePath) throws IllegalArgumentException {
    final String rvmHomePath = RVMPathsSettings.getInstance().getRvmHomePath();
    if (rvmHomePath == null) {
      return null;
    }

    // rvm home, e.g. ~/.rvm/  or /usr/local/rvm
    final String rvmHome = rvmHomePath + "/";
    final String rvmInterpretersFolderPath = rvmHome + Constants.RVM_RUBIES_FOLDER_NAME + "/";
    if (!executablePath.startsWith(rvmInterpretersFolderPath)) {
      return null;
    }

    final String interpreterRelativePath = executablePath.startsWith(rvmInterpretersFolderPath)
                                           ? executablePath.substring(rvmInterpretersFolderPath.length())
                                           : null;
    if (interpreterRelativePath == null) {
      return null;
    }

    // first folder in relative path will be interpreter distributive name
    final String rubyDistName = interpreterRelativePath.substring(0, interpreterRelativePath.indexOf("/"));
    if (rubyDistName == null) {
      throw new IllegalArgumentException("Unable to fetch ruby distributive name for: " + executablePath);
    }
    // base gems path folder
    return new Pair<String, String>(rvmHome + "gems", rubyDistName);
  }

  public static class RubyDistToGemsetTable {
    private final Map<String, List<String>> myRubyDist2Gemset;

    public RubyDistToGemsetTable() {
      this(new HashMap<String, List<String>>());
    }

    public static RubyDistToGemsetTable emptyTable() {
      return new RubyDistToGemsetTable(Collections.<String, List<String>>emptyMap());
    }

    private RubyDistToGemsetTable(Map<String, List<String>> tableImpl) {
      myRubyDist2Gemset = new HashMap<String, List<String>>();
    }

    public void putGemset(final String gemsetName,
                          final String distName) {
      List<String> gemsets = myRubyDist2Gemset.get(distName);
      if (gemsets == null) {
        gemsets = new ArrayList<String>(1);
        myRubyDist2Gemset.put(distName, gemsets);
      }
      // put gemset
      gemsets.add(gemsetName);
    }

    public boolean isEmpty() {
      return myRubyDist2Gemset.isEmpty();
    }

    public List<String> getGemsets(final String distName) {
      return myRubyDist2Gemset.get(distName);
    }

    public Set<String> getDists() {
      return myRubyDist2Gemset.keySet();
    }
   }
}