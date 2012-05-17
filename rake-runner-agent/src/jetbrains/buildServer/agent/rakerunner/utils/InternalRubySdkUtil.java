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

import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.SharedParams;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  static RubySdk createSdk(@NotNull final Map<String, String> runParameters,
                           @NotNull final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    // Check if path to ruby interpreter was explicitly set
    // and calculate corresponding interpreter path

    final RubySdk sdk = SharedParams.fromRunParameters(runParameters).createSdk(buildParameters);

    // Final check that interpreter file exists
    try {
      checkInterpreterPathValid(sdk.getInterpreterPath());
    } catch (SecurityException e) {
      //unknown error
      throw new RunBuildException(e.getMessage(), e);
    }

    return sdk;
  }

  public static void checkInterpreterPathValid(@Nullable final String path) throws RakeTasksBuildService.MyBuildFailureException {
    if (path == null || StringUtil.isEmptyOrSpaces(path) || !FileUtil.checkIfExists(path)) {
      throwInterpratatorDoesntExistError(path);
    }
  }

  @NotNull
  public static String findSystemInterpreterPath(@NotNull final Map<String, String> buildParameters)
    throws RakeTasksBuildService.MyBuildFailureException {
    // find in $PATH
    final String path = OSUtil.findRubyInterpreterInPATH(buildParameters);

    if (path == null) {
      throw new RakeTasksBuildService.MyBuildFailureException("Unable to find Ruby interpreter in PATH.");
    }

    return path;
  }

  //@Nullable
  //public static String getGemsetName(final Map<String, String> runParameters) {
  //  return StringUtil.trimAndNull(runParameters.get(SharedRubyEnvSettings.SHARED_RUBY_RVM_GEMSET_NAME));
  //}

  @NotNull
  private static <T> T throwInterpratatorDoesntExistError(@Nullable final String rubyInterpreterPath)
    throws RakeTasksBuildService.MyBuildFailureException {
    final String msg = "Ruby interpreter '"
                       + String.valueOf(rubyInterpreterPath)
                       + "' doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
    throw new RakeTasksBuildService.MyBuildFailureException(msg);
  }

  //@Nullable
  //static String determineGemset(@NotNull final String rubyInterpreterPath,
  //                              @NotNull final Map<String, String> runParameters)
  //  throws RakeTasksBuildService.MyBuildFailureException {
  //
  //  // RVM - determine gemset
  //  final String rvmGemset = getGemsetName(runParameters);
  //
  //  if (rvmGemset == null) {
  //    return rvmGemset;
  //  }
  //
  //  // validate
  //  final boolean isValid = RVMSupportUtil.isGemsetExists(rvmGemset, rubyInterpreterPath);
  //  if (!isValid) {
  //    final String gemsets = RVMSupportUtil.dumpAvailableGemsets(rubyInterpreterPath);
  //    final String msg = "Gemset '" + rvmGemset + "' isn't defined for ruby interpreter '"
  //                       + rubyInterpreterPath
  //                       + "'. Please create the gemset at first.\n"
  //                       + (gemsets == null ? "" : "\nAvailable gemsets: " + gemsets);
  //    throw new RakeTasksBuildService.MyBuildFailureException(msg);
  //  }
  //  return rvmGemset;
  //}

  public static boolean isRuby19Interpreter(@NotNull final RubySdk sdk,
                                            @Nullable final Map<String, String> buildConfEnvironment) {
    boolean isRuby19 = false;
    final RunnerUtil.Output rubyVersionResult =
      RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment, RUBY_VERSION_SCRIPT);
    final String stdOut = rubyVersionResult.getStdout();
    if (stdOut.contains("1.9.")) {
      isRuby19 = true;
    }
    return isRuby19;
  }

  public static boolean isJRubyInterpreter(@NotNull final RubySdk sdk,
                                           @Nullable final Map<String, String> buildConfEnvironment) {
    final String interpPath = toSystemIndependentName(sdk.getInterpreterPath());
    if (interpPath.endsWith("/jruby")) {
      return true;
    }
    boolean isJRuby = false;
    final RunnerUtil.Output rubyVersionResult =
      RubySDKUtil.executeScriptFromSource(sdk, buildConfEnvironment, RUBY_PLATFORM_SCRIPT);
    final String stdOut = rubyVersionResult.getStdout();
    if (stdOut.contains("java")) {
      isJRuby = true;
    }
    return isJRuby;
  }

  public static RunnerUtil.Output getLoadPaths(@NotNull final RubySdk sdk,
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
