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

import java.io.File;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.SharedRubyEnvSettings;
import jetbrains.buildServer.agent.ruby.RubyLightweightSdk;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRubyLightweightSdkImpl;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;

/**
 * @author Roman.Chernyatchik
 */
public class InternalRubySdkUtil {
  public static final String RUBY_VERSION_SCRIPT = "print RUBY_VERSION";
  public static final String RUBY_PLATFORM_SCRIPT = "print RUBY_PLATFORM";
  @NonNls
  public static final String GET_LOAD_PATH_SCRIPT = "puts $LOAD_PATH";
  public static final String RUBY19_DISABLE_GEMS_OPTION = "--disable-gems";

  @NotNull
  static RubyLightweightSdk createLightWeightSdk(@NotNull final Map<String, String> runParameters,
                                                 @NotNull final Map<String, String> buildParameters)
      throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {


    // Check if path to ruby interpreter was explicitly set
    // and calculate corresponding interpreter path
    final RubyLightweightSdk sdk = determineRubyLightweightSdk(runParameters, buildParameters);

    // Final check that interpreter file exists
    final File rubyInterpreterFile = new File(sdk.getInterpreterPath());
    try {
      if (rubyInterpreterFile == null || !FileUtil.checkIfFileExists(rubyInterpreterFile)) {
        return throwInterpratatorDoesntExistError(sdk.getInterpreterPath());
      }
    } catch (SecurityException e) {
      //unknown error
      throw new RunBuildException(e.getMessage(), e);
    }

    // Check if interpreter is RVM based
    if (sdk.isRvmSdk() && !sdk.isSystem()) {
      // TODO: check is gemset validaton needed
    }
    return sdk;
  }

  @NotNull
  static RubyLightweightSdk determineRubyLightweightSdk(@NotNull final Map<String, String> runParameters,
                                                        @NotNull final Map<String, String> buildParameters)
      throws RakeTasksBuildService.MyBuildFailureException {

    String sharedRubyInterpreterSetting = runParameters.get(SharedRubyEnvSettings.SHARED_RUBY_INTERPRETER_PATH);
    // custom
    if (sharedRubyInterpreterSetting != null && FileUtil.checkIfExists(sharedRubyInterpreterSetting)) {
      return new jetbrains.buildServer.agent.ruby.impl.RubyLightweightSdkImpl(sharedRubyInterpreterSetting, false);
    }

    if (sharedRubyInterpreterSetting == null) {
      sharedRubyInterpreterSetting = runParameters.get(SharedRubyEnvSettings.SHARED_RUBY_RVM_SDK_NAME);
    }

    if (StringUtil.isEmptyOrSpaces(sharedRubyInterpreterSetting)) {
      return new jetbrains.buildServer.agent.ruby.impl.RubyLightweightSdkImpl(findSystemInterpreterPath(buildParameters), true);
    }

    // probably rvm short sdk name
    // short name shouldn't contain slashes
    if (!sharedRubyInterpreterSetting.contains("/")
        && !sharedRubyInterpreterSetting.contains("\\")) {

      // at first lets check that it isn't "system" interpreter
      if (RVMSupportUtil.isSystemRuby(sharedRubyInterpreterSetting)) {
        return new RVMRubyLightweightSdkImpl(findSystemInterpreterPath(buildParameters), "system", true, null);
      }

      // build  dist/gemsets table, match ref with dist. name
      final String rvmGemset = getGemsetName(runParameters);

      final String distName = RVMSupportUtil.determineSuitableRVMSdkDist(sharedRubyInterpreterSetting, rvmGemset);

      if (distName != null) {
        return new RVMRubyLightweightSdkImpl(RVMSupportUtil.suggestInterpretatorPath(distName), distName, false, rvmGemset);
      }
      final String msg = "Gemset '" + rvmGemset + "' isn't defined for Ruby interpreter '"
          + sharedRubyInterpreterSetting
          + "' or the interpreter doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    }

    return throwInterpratatorDoesntExistError(sharedRubyInterpreterSetting);
  }

  @NotNull
  static String findSystemInterpreterPath(@NotNull final Map<String, String> buildParameters)
      throws RakeTasksBuildService.MyBuildFailureException {
    // find in $PATH
    final String path = OSUtil.findRubyInterpreterInPATH(buildParameters);
    if (path != null) {
      return path;
    }

    final String msg = "Unable to find Ruby interpreter in PATH.";
    throw new RakeTasksBuildService.MyBuildFailureException(msg);
  }

  @Nullable
  static String getGemsetName(final Map<String, String> runParameters) {
    final String uiRVMGemsetString = runParameters.get(SharedRubyEnvSettings.SHARED_RUBY_RVM_GEMSET_NAME);
    return !StringUtil.isEmptyOrSpaces(uiRVMGemsetString) ? uiRVMGemsetString.trim() : null;
  }

  @NotNull
  static <T> T throwInterpratatorDoesntExistError(final String rubyInterpreterPath)
      throws RakeTasksBuildService.MyBuildFailureException {
    final String msg = "Ruby interpreter '"
        + rubyInterpreterPath
        + "' doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
    throw new RakeTasksBuildService.MyBuildFailureException(msg);
  }

  @Nullable
  static String determineGemset(@NotNull final String rubyInterpreterPath,
                                @NotNull final Map<String, String> runParameters)
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
          + (gemsets == null ? "" : "\nAvailable gemsets: " + gemsets);
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    }
    return rvmGemset;
  }

  static boolean isRuby19Interpreter(@NotNull final RubySdk sdk,
                                     final Map<String, String> buildConfEnvironment) {
    boolean isRuby19 = false;
    final RubyScriptRunner.Output rubyVersionResult =
        RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment, RUBY_VERSION_SCRIPT);
    final String stdOut = rubyVersionResult.getStdout();
    if (stdOut.contains("1.9.")) {
      isRuby19 = true;
    }
    return isRuby19;
  }

  static boolean isJRubyInterpreter(@NotNull final RubySdk sdk,
                                    final Map<String, String> buildConfEnvironment) {
    final String interpPath = toSystemIndependentName(sdk.getInterpreterPath());
    if (interpPath.endsWith("/jruby")) {
      return true;
    }
    boolean isJRuby = false;
    final RubyScriptRunner.Output rubyVersionResult =
        RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment, RUBY_PLATFORM_SCRIPT);
    final String stdOut = rubyVersionResult.getStdout();
    if (stdOut.contains("java")) {
      isJRuby = true;
    }
    return isJRuby;
  }

  static RubyScriptRunner.Output getLoadPaths(@NotNull final RubySdk sdk,
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
