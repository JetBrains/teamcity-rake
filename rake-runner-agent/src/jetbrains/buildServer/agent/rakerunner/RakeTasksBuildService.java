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

import com.intellij.util.containers.HashMap;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.runner.BuildFileRunnerUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import static jetbrains.buildServer.runner.BuildFileRunnerConstants.BUILD_FILE_PATH_KEY;

/**
 * @author Roman.Chernyatchik
 */
public class RakeTasksBuildService extends BuildServiceAdapter implements RakeRunnerConstants {
  private final Set<File> myFilesToDelete = new HashSet<File>();
  private final String RSPEC_RUNNER_OPTIONS_REQUIRE_KEY = "--require";
  private final String RSPEC_RUNNER_OPTIONS_FORMATTER_PATH = "teamcity/spec/runner/formatter/teamcity/formatter";
  private final String RSPEC_RUNNERR_OPTIONS_FORMATTER_KEY = "--format";
  private final String RSPEC_RUNNERR_OPTIONS_FORMATTER_CLASS = "Spec::Runner::Formatter::TeamcityFormatter";
  private static final String RAKE_ERROR_TYPE = "RAKE_ERROR";

  private final String CUCUMBER_RUNNER_OPTIONS_EXPAND_KEY = "--expand";
  private final String CUCUMBER_RUNNER_OPTIONS_FORMAT_KEY = "--format";
  private final String CUCUMBER_RUNNER_OPTIONS_FORMAT_CLASS = "Teamcity::Cucumber::Formatter";


  @NotNull
  @Override
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    List<String> arguments = new ArrayList<String>();
    final Map<String, String> runParams = new HashMap<String, String>(getRunnerParameters());
    final Map<String, String> buildParams = new HashMap<String, String>(getBuildParameters().getAllParameters());

    // apply options converter
    SupportedTestFramework.convertOptionsIfNecessary(runParams);

    // runParams - all server-ui options
    // buildParams - system properties (system.*), environment vars (env.*)

    final boolean inDebugMode = ConfigurationParamsUtil.isParameterEnabled(buildParams, DEBUG_PROPERTY);
    final Map<String, String> buildEnvVars = getBuildParameters().getEnvironmentVariables();
    final Map<String, String> runnerEnvParams = new HashMap<String, String>(buildEnvVars);

    final File buildFile = getBuildFile(runParams);

    try {
      // Interpreter
      final RubySdk sdk = RubySdkImpl.createAndSetupSdk(runParams, buildParams, buildEnvVars);

      // Patch env for RVM
      // TODO - system wide rvm support
      RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, runnerEnvParams);

      // SDK patch
      addTestRunnerPatchFiles(sdk, runParams, buildParams, runnerEnvParams);

      // attached frameworks info
      if (SupportedTestFramework.isAnyFrameworkActivated(runParams)) {
        runnerEnvParams.put(RAKERUNNER_USED_FRAMEWORKS_KEY,
                   SupportedTestFramework.getActivatedFrameworksConfig(runParams));

      }

      // TODO bundler support

      // track invoke/execute stages
      // TODO - stages are not visible !!!!
      if (ConfigurationParamsUtil.isTraceStagesOptionEnabled(runParams)) {
        runnerEnvParams.put(RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY, Boolean.TRUE.toString());
      }

      // Rake runner script
      final String rakeRunnerPath;
      final String customRakeRunnerScript = buildParams.get(CUSTOM_RAKERUNNER_SCRIPT);
      if (!TextUtil.isEmpty(customRakeRunnerScript)) {
        // use custom runner
        rakeRunnerPath = customRakeRunnerScript;
      } else {
        // default one
        rakeRunnerPath = RubyProjectSourcesUtil.getRakeRunnerPath();
      }
      arguments.add(rakeRunnerPath);

      // Rake gem version
      addGemVersionAttribute(arguments, RAKE_GEM_VERSION_PROPERTY, buildParams);

      // Rake options
      // Custom Rakefile if specified
      if (buildFile != null) {
        arguments.add(RAKE_CMDLINE_OPTIONS_RAKEFILE);
        arguments.add(buildFile.getAbsolutePath());
      }

      // Other arguments
      final String otherArgsString = runParams.get(SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY);
      if (!TextUtil.isEmptyOrWhitespaced(otherArgsString)) {
        addCmdlineArguments(arguments, otherArgsString);
      }

      // Tasks names
      final String tasks_names = runParams.get(SERVER_UI_RAKE_TASKS_PROPERTY);
      if (!PropertiesUtil.isEmptyOrNull(tasks_names)) {
        addCmdlineArguments(arguments, tasks_names);
      }

      // test-unit based
      // TODO - test-unit gem version via system properties (use TEST_UNIT_GEM_VERSION_PROPERTY)

      // rspec
      attachRSpecFormatterIfNeeded(runParams, runnerEnvParams);

      // cucumber
      attachCucumberFormatterIfNeeded(runParams, runnerEnvParams);

      if (inDebugMode) {
        getLogger().message("\n{RAKE RUNNER DEBUG}: CommandLine : \n"
                            + sdk.getPresentableName()
                            + " "
                            + arguments.toString());
        getLogger().message("\n{RAKE RUNNER DEBUG}: Working Directory: [" + getWorkingDirectory() + "]");
      }

      return new SimpleProgramCommandLine(runnerEnvParams,
                                          getWorkingDirectory().getAbsolutePath(),
                                          sdk.getInterpreterPath(),
                                          arguments);
    } catch (MyBuildFailureException e) {
      getLogger().internalError(RAKE_ERROR_TYPE, e.getTitle(), e);
      throw new RunBuildException(e.getMessage());
    }
  }

  /**
   * Specify gem version attribute if property is set
   * @param arguments Cmdline arguments
   * @param gemVersionProperty Property name
   * @param buildParams  Build params
   */
  private void addGemVersionAttribute(final List<String> arguments,
                                      final String gemVersionProperty,
                                      final Map<String, String> buildParams) {
    final String rakeGemVersion = buildParams.get(gemVersionProperty);
    if (!StringUtil.isEmpty(rakeGemVersion)) {
      arguments.add("_" + rakeGemVersion + "_");
    }
  }

  @Override
  public void afterProcessFinished() {
    // Remove tmp files
    for (File file : myFilesToDelete) {
      jetbrains.buildServer.util.FileUtil.delete(file);
    }
    myFilesToDelete.clear();
  }

  private void attachRSpecFormatterIfNeeded(final Map<String, String> runParams,
                                            final Map<String, String> env) {
    // TODO - rspec gem version via system properties

    //attach RSpec formatter only if spec reporter enabled
    if (SupportedTestFramework.RSPEC.isActivated(runParams)) {
      final StringBuilder buff = new StringBuilder();

      final String userSpecOpts = runParams.get(SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY);
      if (!TextUtil.isEmpty(userSpecOpts)) {
        buff.append(userSpecOpts.trim()).append(' ');
      }
      buff.append(RSPEC_RUNNER_OPTIONS_REQUIRE_KEY).append(' ');
      buff.append(RSPEC_RUNNER_OPTIONS_FORMATTER_PATH).append(' ');
      buff.append(RSPEC_RUNNERR_OPTIONS_FORMATTER_KEY).append(' ');
      buff.append(RSPEC_RUNNERR_OPTIONS_FORMATTER_CLASS);

      final String specOpts = buff.toString();

      // Log for user
      getLogger().message("RSpec Options: " + specOpts);

      // Set env variable
      env.put(RAKE_RSPEC_OPTS_PARAM_NAME, specOpts);
    }
  }

  private void attachCucumberFormatterIfNeeded(
    final Map<String, String> runParams,
    final Map<String, String> env) {

    // TODO - cucumber gem version via system properties

    //attach Cucumber formatter only if cucumber reporter enabled
    if (SupportedTestFramework.CUCUMBER.isActivated(runParams)) {
      final StringBuilder buff = new StringBuilder();

      //TODO use additional options when cucumber will support it!
      //cmd.addParameter(RAKE_CUCUMBER_OPTS_PARAM_NAME + "=" + CUCUMBER_RUNNER_INIT_OPTIONS);

      final String userCucumberOpts = runParams.get(SERVER_UI_RAKE_CUCUMBER_OPTS_PROPERTY);
      if (!TextUtil.isEmpty(userCucumberOpts)) {
        buff.append(userCucumberOpts.trim()).append(' ');
      }
      buff.append(CUCUMBER_RUNNER_OPTIONS_EXPAND_KEY).append(' ');
      buff.append(CUCUMBER_RUNNER_OPTIONS_FORMAT_KEY).append(' ');
      buff.append(CUCUMBER_RUNNER_OPTIONS_FORMAT_CLASS);

      final String cucumberOpts = buff.toString();

      // Log for user
      getLogger().message("Cucumber Options: " + cucumberOpts);

      // Set env variable
      env.put(RAKE_CUCUMBER_OPTS_PARAM_NAME, cucumberOpts);
    }
  }

  private void addTestRunnerPatchFiles(@NotNull final RubySdk sdk,
                                       final Map<String, String> runParams,
                                       final Map<String, String> buildParams,
                                       final Map<String, String> runnerEnvParams)
      throws MyBuildFailureException, RunBuildException {


    final StringBuilder buff = new StringBuilder();

    // common part - for rake taks and tests
    buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_Common());

    // Enable Test::Unit patch for : test::unit, test::spec and shoulda frameworks
    if (SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams)) {
      buff.append(File.pathSeparatorChar);
      buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_TestUnit());

      // due to patching loadpath we replace original autorunner but it is used buy our tests runner
      runnerEnvParams.put(ORIGINAL_SDK_AUTORUNNER_PATH_KEY,
                 RubySDKUtil.getSDKTestUnitAutoRunnerScriptPath(sdk, buildParams));
      runnerEnvParams.put(ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY,
                 RubySDKUtil.getSDKTestUnitTestRunnerMediatorScriptPath(sdk, buildParams));
    }

    // for bdd frameworks
    if (SupportedTestFramework.CUCUMBER.isActivated(runParams)
        || SupportedTestFramework.RSPEC.isActivated(runParams)) {
      buff.append(File.pathSeparatorChar);
      buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_Bdd());
    }

    // patch loadpath
    runnerEnvParams.put(RUBYLIB_ENVIRONMENT_VARIABLE,
               OSUtil.appendToRUBYLIBEnvVariable(buff.toString()));
  }

  private void addCmdlineArguments(@NotNull final List<String> argsList, @NotNull final String argsString) {
    final List<String> stringList = StringUtil.splitHonorQuotes(argsString, ' ');
    for (String arg : stringList) {
      argsList.add(TextUtil.stripDoubleQuoteAroundValue(arg));
    }
  }

  @Nullable
  private File getBuildFile(Map<String, String> runParameters) throws RunBuildException {
    final File buildFile;
    if (BuildFileRunnerUtil.isCustomBuildFileUsed(runParameters)) {
      buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
      myFilesToDelete.add(buildFile);
    } else {
      final String buildFilePath = runParameters.get(BUILD_FILE_PATH_KEY);
      if (PropertiesUtil.isEmptyOrNull(buildFilePath)) {
        //use rake defaults
        buildFile = null;
      } else {
        buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
      }
    }
    return buildFile;
  }

  public static class MyBuildFailureException extends Exception {
    private final String msg;
    private final String title;

    public MyBuildFailureException(@NotNull final String msg,
                                   @NotNull final String title) {
      this.msg = msg;
      this.title = title;
    }

    @Override
    public String getMessage() {
      return msg;
    }

    public String getTitle() {
      return title;
    }
  }

}
