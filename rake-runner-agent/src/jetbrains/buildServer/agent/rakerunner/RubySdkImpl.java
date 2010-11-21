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

package jetbrains.buildServer.agent.rakerunner;

import java.io.File;
import java.util.*;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import static jetbrains.buildServer.rakerunner.RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT;

/**
 * @author Roman.Chernyatchik
 */
public class RubySdkImpl implements RubySdk {
  private static final String GET_GEM_PATHS_SCRIPT =  "require 'rubygems'; puts Gem.path";
  private static final String RUBY_VERSION_SCRIPT = "print RUBY_VERSION";
  @NonNls
  public static final String GET_LOAD_PATH_SCRIPT =  "puts $LOAD_PATH";
  private static final String RUBY19_DISABLE_GEMS_OPTION = "--disable-gems";

  private final String myInterpreterPath;
  private final boolean myIsRvmSdk;
  private final String myGemsetName;
  private String[] myGemPaths;
  private boolean myIsRuby19;
  private RubyScriptRunner.Output myGemPathsLog;
  private RubyScriptRunner.Output myLoadPathsLog;
  private String[] myLoadPaths;

  protected RubySdkImpl(@NotNull final String interpreterPath,
                        final boolean isRvmSdk,
                        final String gemsetName) {

    myInterpreterPath = interpreterPath;
    myIsRvmSdk = isRvmSdk;
    myGemsetName = gemsetName;
  }

  @NotNull
  public String[] getGemPaths() {
    return myGemPaths;
  }

  @NotNull
  public boolean isRuby19() {
    return myIsRuby19;
  }

  @NotNull
  public RubyScriptRunner.Output getGemPathsFetchLog() {
    return myGemPathsLog;
  }

  @NotNull
  public RubyScriptRunner.Output getLoadPathsFetchLog() {
    return myLoadPathsLog;
  }

  @NotNull
  public String[] getLoadPath() {
    return myLoadPaths;
  }

  public static RubySdk createAndSetupSdk(final Map<String, String> runParameters,
                                          final Map<String, String> buildParameters,
                                          final Map<String, String> buildConfEnvironment)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    // create
    final RubySdkImpl sdk = createSdkImpl(runParameters, buildParameters);

    // initialize

    // language level
    final boolean isRuby19 = isRuby19Interpreter(sdk, buildConfEnvironment);
    sdk.myIsRuby19 = isRuby19;

    // gem paths
    final RubyScriptRunner.Output gemPathsResult = RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment,
                                                                                       GET_GEM_PATHS_SCRIPT);
    sdk.myGemPathsLog = gemPathsResult;
    sdk.myGemPaths = TextUtil.splitByLines(gemPathsResult.getStdout());

    // load path
    final RubyScriptRunner.Output loadPathResult = getLoadPaths(sdk, buildConfEnvironment, isRuby19);
    sdk.myLoadPathsLog = loadPathResult;
    sdk.myLoadPaths = TextUtil.splitByLines(loadPathResult.getStdout());

    return sdk;
  }

  @NotNull
  public String getInterpreterPath() {
    return myInterpreterPath;
  }

  public boolean isRVMSdk() {
    return myIsRvmSdk;
  }

  @Nullable
  public String getRvmGemsetName() {
    return myGemsetName;
  }

  @NotNull
  public String getPresentableName() {
    if (isRVMSdk()) {
      final String gemsetName = getRvmGemsetName();
      return myInterpreterPath + (gemsetName != null ? "[" + RVMSupportUtil.getGemsetSeparator() + gemsetName + "]" : "");
    } else {
      return myInterpreterPath;
    }
  }


  @NotNull
  private static RubySdkImpl createSdkImpl(final Map<String, String> runParameters,
                                           final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {


    // Check if path to ruby interpreter was explicitly set
    // and calculate corresponding interpreter path
    final String rubyInterpreterPath = determineRubyInterpreterPath(runParameters, buildParameters);

    // Final check that interpreter file exists
    final File rubyInterpreter = rubyInterpreterPath != null ? new File(rubyInterpreterPath) : null;
    try {
      if (rubyInterpreter == null || !rubyInterpreter.exists() || !rubyInterpreter.isFile()) {
        return throwInterpratatorDoesntExistError(rubyInterpreterPath);
      }
    } catch (SecurityException e) {
      //unknown error
      throw new RunBuildException(e.getMessage(), e);
    }

    // Check if interpreter is RVM based
    if (!RVMSupportUtil.isRVMInterpreter(rubyInterpreterPath)) {
      // not RVM
      return new RubySdkImpl(rubyInterpreterPath, false, null);
    }

    // RVM - determine gemset
    final String rvmGemset = determineGemset(rubyInterpreterPath, runParameters);

    // TODO: bundler gem paths!
    return new RubySdkImpl(rubyInterpreterPath, true, rvmGemset);
  }

  private static RubySdkImpl throwInterpratatorDoesntExistError(final String rubyInterpreterPath)
    throws RakeTasksBuildService.MyBuildFailureException {
    final String msg = "Ruby interpreter '"
                       + rubyInterpreterPath
                       + "' doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
    throw new RakeTasksBuildService.MyBuildFailureException(msg, RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
  }

  private static String determineGemset(@NotNull final String rubyInterpreterPath,
                                        final Map<String, String> runParameters)
    throws RakeTasksBuildService.MyBuildFailureException {

    // RVM - determine gemset
    final String rvmGemset = getGemsetName(runParameters);

    if (rvmGemset == null) {
      return rvmGemset;
    }

    // validate
    final boolean isValid = RVMSupportUtil.isGemsetExists(rvmGemset, rubyInterpreterPath);
    if (!isValid) {
      final String gemsets = RVMSupportUtil.dumpAvailableGemsets(rubyInterpreterPath);
      final String msg = "Gemset '" + rvmGemset + "' isn't defined for ruby interpreter '"
                         + rubyInterpreterPath
                         + "'. Please create the gemset at first.\n"
                         +  (gemsets == null ? "" : "\nAvailable gemsets: " + gemsets);
      throw new RakeTasksBuildService.MyBuildFailureException(msg, RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }
    return rvmGemset;
  }

  @NotNull
  private static String determineRubyInterpreterPath(final Map<String, String> runParameters,
                                                     final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException {

    final String uiRubyInterpreterSetting = runParameters.get(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER);

    // use default interpreter
    if (PropertiesUtil.isEmptyOrNull(uiRubyInterpreterSetting)) {
      return findSystemInterpreterPath(buildParameters);
    }

    // custom
    if (FileUtil.checkIfExists(uiRubyInterpreterSetting)) {
      return uiRubyInterpreterSetting;
    }

    // probably rvm short sdk name

    // short name shouldn't contain slashes
    if (!uiRubyInterpreterSetting.contains("/")
        && !uiRubyInterpreterSetting.contains("\\")) {

      // at first lets check that it isn't "system" interpreter
      if (RVMSupportUtil.isSystemRuby(uiRubyInterpreterSetting)) {
        return findSystemInterpreterPath(buildParameters);
      }

      // build  dist/gemsets table, match ref with dist. name
      final String rvmGemset = getGemsetName(runParameters);

      final String distName = RVMSupportUtil.determineSuitableRVMSdkDist(uiRubyInterpreterSetting, rvmGemset);
      if (distName != null) {
        return RVMSupportUtil.suggestInterpretatorPath(distName);
      }
      final String msg = "Gemset '" + rvmGemset + "' isn't defined for Ruby interpreter '"
                         + uiRubyInterpreterSetting
                         + "' or the interpreter doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg, RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }

    throwInterpratatorDoesntExistError(uiRubyInterpreterSetting);
    return null;
  }

  private static String findSystemInterpreterPath(final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException {
    // find in $PATH
    final String path = OSUtil.findRubyInterpreterInPATH(buildParameters);
    if (path != null) {
      return path;
    }

    final String msg = "Unable to find Ruby interpreter in PATH.";
    throw new RakeTasksBuildService.MyBuildFailureException(msg, RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
  }

  @Nullable
  private static String getGemsetName(final Map<String, String> runParameters) {
    final String uiRVMGemsetString = runParameters.get(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME);
    return !PropertiesUtil.isEmptyOrNull(uiRVMGemsetString) ? uiRVMGemsetString.trim() : null;
  }

  private static boolean isRuby19Interpreter(@NotNull final RubySdk sdk,
                                             final Map<String, String> buildConfEnvironment) {
    boolean isRuby19 = false;
    final RubyScriptRunner.Output rubyVersionResult =
      RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment, RUBY_VERSION_SCRIPT);
    final String stdOut = rubyVersionResult.getStdout();
    if (stdOut != null && stdOut.contains("1.9.")) {
      isRuby19 = true;
    }
    return isRuby19;
  }

  private static RubyScriptRunner.Output getLoadPaths(@NotNull final RubySdk sdk,
                                                      final Map<String, String> buildConfEnvironment,
                                                      final boolean ruby19) {
    // LOAD_PATH way
    final String[] rubyArgs;
    if (ruby19) {
      // filter gem paths in case of Ruby 1.9 (use --disable-gems)
      rubyArgs = new String[]{RUBY19_DISABLE_GEMS_OPTION};
    } else {
      rubyArgs = new String[0];
    }

    return RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment,
                                               GET_LOAD_PATH_SCRIPT, rubyArgs);
  }
}
