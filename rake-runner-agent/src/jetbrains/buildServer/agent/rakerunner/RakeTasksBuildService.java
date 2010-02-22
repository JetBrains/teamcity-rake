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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.runner.BuildFileRunnerUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static jetbrains.buildServer.runner.BuildFileRunnerConstants.BUILD_FILE_PATH_KEY;

/**
 * @author Roman.Chernyatchik
 */
public class RakeTasksBuildService extends CommandLineBuildService implements RakeRunnerConstants {
  private final Set<File> myFilesToDelete = new HashSet<File>();
  private final String RSPEC_RUNNER_OPTIONS_REQUIRE = "--require 'teamcity/spec/runner/formatter/teamcity/formatter'";
  private final String RSPEC_RUNNERR_OPTIONS_FORMATTER = "--format Spec::Runner::Formatter::TeamcityFormatter:matrix";
  private final String CUCUMBER_RUNNER_INIT_OPTIONS = "--format Teamcity::Cucumber::Formatter --expand";


  @NotNull
  @Override
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    List<String> arguments = new ArrayList<String>();
    Map<String, String> runParams = new HashMap<String, String>(getBuild().getRunnerParameters());
    Map<String, String> buildParams = new HashMap<String, String>(getBuild().getBuildParameters().getAllParameters());

    // apply options converter
    SupportedTestFramework.convertOptionsIfNecessary(runParams);

    // runParams - all server-ui options
    // buildParams - system properties (system.*), environment vars (env.*)

    final boolean inDebugMode = ConfigurationParamsUtil.isParameterEnabled(buildParams, DEBUG_PROPERTY);
    final HashMap<String, String> envMap = new HashMap<String, String>(getBuild().getBuildParameters().getEnvironmentVariables());
    String exePath;

    final File buildFile = getBuildFile(runParams);

    try {

      // SDK patch
      addTestRunnerPatchFiles(runParams, buildParams, envMap);

      // attached frameworks info
      if (SupportedTestFramework.isAnyFrameworkActivated(runParams)) {
        envMap.put(RAKERUNNER_USED_FRAMEWORKS_KEY,
                   SupportedTestFramework.getActivatedFrameworksConfig(runParams));

      }

      // set runner mode to "buildserver" mode
      envMap.put(RAKE_MODE_KEY, RAKE_MODE_BUILDSERVER);

      // track invoke/execute stages
      if (ConfigurationParamsUtil.isTraceStagesOptionEnabled(runParams)) {
        envMap.put(RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY, Boolean.TRUE.toString());
      }

      exePath = ConfigurationParamsUtil.getRubyInterpreterPath(runParams, buildParams);

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

      // rspec
      attachRSpecFormatterIfNeeded(arguments, runParams);

      // cucumber
      attachCucumberFormatterIfNeeded(arguments, runParams);

      if (inDebugMode) {
        getLogger().message("\n{RAKE RUNNER DEBUG}: CommandLine : \n" + exePath + " " + arguments.toString());
      }

      if (inDebugMode) {
        getLogger().message("\n{RAKE RUNNER DEBUG}: Working Directory: [" + getBuild().getWorkingDirectory() + "]");
      }

      getLogger().message("Environment: " + envMap);

      return new SimpleProgramCommandLine(envMap, getBuild().getWorkingDirectory().getAbsolutePath(), exePath, arguments);
    } catch (MyBuildFailureException e) {
      getLogger().buildFailureDescription(e.getTitle());
      throw new RunBuildException(e.getMessage());
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

  private void attachRSpecFormatterIfNeeded(final List<String> argsList,
                                            final Map<String, String> runParams) {
    //attach RSpec formatter only if spec reporter enabled
    if (SupportedTestFramework.RSPEC.isActivated(runParams)) {
      final String specRunnerInitString = RSPEC_RUNNER_OPTIONS_REQUIRE + " " + RSPEC_RUNNERR_OPTIONS_FORMATTER;
      String specOpts = runParams.get(SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY);
      if (TextUtil.isEmpty(specOpts)) {
        specOpts = specRunnerInitString;
      } else {
        specOpts = specOpts.trim() + " " + specRunnerInitString;
      }

      argsList.add(RAKE_RSPEC_OPTS_PARAM_NAME + "=" + specOpts.trim());
    }
  }

  private void attachCucumberFormatterIfNeeded(final List<String> arguments,
                                               final Map<String, String> runParams) {
    //attach Cucumber formatter only if cucumber reporter enabled
    if (SupportedTestFramework.CUCUMBER.isActivated(runParams)) {
      //TODO use additional options when cucumber will support it!
      //cmd.addParameter(RAKE_CUCUMBER_OPTS_PARAM_NAME + "=" + CUCUMBER_RUNNER_INIT_OPTIONS);

      final String cucumberRunnerInitString = CUCUMBER_RUNNER_INIT_OPTIONS;
      String cucumberOpts = runParams.get(SERVER_UI_RAKE_CUCUMBER_OPTS_PROPERTY);
      if (TextUtil.isEmpty(cucumberOpts)) {
        cucumberOpts = cucumberRunnerInitString;
      } else {
        cucumberOpts = cucumberOpts.trim() + " " + cucumberRunnerInitString;
      }

      arguments.add(RAKE_CUCUMBER_OPTS_PARAM_NAME + "=" + cucumberOpts.trim());
    }
  }

  private void addTestRunnerPatchFiles(final Map<String, String> runParams,
                                       final Map<String, String> buildParams,
                                       final Map<String, String> envMap)
      throws MyBuildFailureException, RunBuildException {


    final StringBuilder buff = new StringBuilder();

    // common part - for rake taks and tests
    buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_Common());

    // Enable Test::Unit patch for : test::unit, test::spec and shoulda frameworks
    if (SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams)) {
      buff.append(File.pathSeparatorChar);
      buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_TestUnit());

      // due to patching loadpath we replace original autorunner but it is used buy our tests runner
      envMap.put(ORIGINAL_SDK_AUTORUNNER_PATH_KEY,
                 RubySDKUtil.getSDKTestUnitAutoRunnerScriptPath(runParams, buildParams));
      envMap.put(ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY,
                 RubySDKUtil.getSDKTestUnitTestRunnerMediatorScriptPath(runParams, buildParams));
    }

    // for bdd frameworks
    if (SupportedTestFramework.CUCUMBER.isActivated(runParams)
        || SupportedTestFramework.RSPEC.isActivated(runParams)) {
      buff.append(File.pathSeparatorChar);
      buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_Bdd());
    }

    // patch loadpath
    envMap.put(RUBYLIB_ENVIRONMENT_VARIABLE,
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
