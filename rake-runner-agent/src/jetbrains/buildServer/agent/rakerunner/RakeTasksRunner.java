/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.util.Key;
import com.intellij.util.containers.HashMap;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.agent.runner.GenericProgramRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import static jetbrains.buildServer.runner.BuildFileRunnerConstants.BUILD_FILE_PATH_KEY;
import jetbrains.buildServer.runner.BuildFileRunnerUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class RakeTasksRunner extends GenericProgramRunner implements RakeRunnerConstants {
  private final Set<File> myFilesToDelete = new HashSet<File>();
  private final String RSPEC_RUNNER_OPTIONS_REQUIRE = "--require 'teamcity/spec/runner/formatter/teamcity/formatter'";
  private final String RSPEC_RUNNERR_OPTIONS_FORMATTER = "--format Spec::Runner::Formatter::TeamcityFormatter:matrix";

  @NonNls
  public String getType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }

  public boolean canRun(final BuildAgentConfiguration agentConfiguration) {
      return true;
  }

  @Override
  protected void buildCommandLine(@NotNull final GeneralCommandLine cmd,
                                  @NotNull final File soourcesRootDir,
                                  @NotNull final Map<String, String> runParams,
                                  @NotNull final Map<String, String> buildParams)
      throws IOException, RunBuildException {

    // runParams - all server-ui options
    // buildParams - system properties (system.*), environment vars (env.*)

    final boolean inDebugMode = ConfigurationParamsUtil.isParameterEnabled(buildParams, RakeRunnerConstants.DEBUG_PROPERTY);

    final File buildFile = getBuildFile(runParams);
    try {
      // Special rake runner Environment properties
      final HashMap<String, String> envMap = new HashMap<String, String>();

// SDK patch
      addTestRunnerPatchFiles(runParams, buildParams, envMap);


// Other runner ENV parameters
      // set runner mode to "buildserver" mode
      envMap.put(RAKE_MODE_KEY, RAKE_MODE_BUILDSERVER);

      // track invoke/execute stages
      if (ConfigurationParamsUtil.isParameterEnabled(runParams, SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED)) {
        envMap.put(RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY, Boolean.TRUE.toString());
      }

      cmd.setEnvParams(envMap);

// CommandLine options

      // Ruby interpreter
      cmd.setExePath(ConfigurationParamsUtil.getRubyInterpreterPath(runParams, buildParams));

      // Rake runner script
      cmd.addParameter(RubyProjectSourcesUtil.getRakeRunnerPath());

      // Rake options
      // Custom Rakefile if specified
      if (buildFile != null) {
        cmd.addParameter(RAKE_CMDLINE_OPTIONS_RAKEFILE);
        cmd.addParameter(buildFile.getAbsolutePath());
      }
      // Other arguments
      final String otherArgsString = runParams.get(SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY);
      if (!TextUtil.isEmptyOrWhitespaced(otherArgsString)) {
        addCmdlineArguments(cmd, otherArgsString);
      }

      // Tasks names
      final String tasks_names = runParams.get(SERVER_UI_RAKE_TASKS_PROPERTY);
      if (!PropertiesUtil.isEmptyOrNull(tasks_names)) {
        addCmdlineArguments(cmd, tasks_names);
      }

      final String specRunnerInitString = RSPEC_RUNNER_OPTIONS_REQUIRE + " " + RSPEC_RUNNERR_OPTIONS_FORMATTER;
      String specOpts = runParams.get(SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY);
      if (TextUtil.isEmpty(specOpts)) {
        specOpts = specRunnerInitString;
      } else {
        specOpts = specOpts.trim() + " " + specRunnerInitString;
      }

      cmd.addParameter(RAKE_RSPEC_OPTS_PARAM_NAME + "=" + specOpts.trim());

      if (inDebugMode) {
        getBuildLogger().message("\n{RAKE RUNNER DEBUG}: CommandLine : \n" + cmd.getCommandLineString());
      }
    } catch (MyBuildFailureException e) {
      failRakeTaskBuild(e);
    }

    if (inDebugMode) {
      getBuildLogger().message("\n{RAKE RUNNER DEBUG}: Working Directory: [" + soourcesRootDir.getCanonicalPath() + "]");
    }
  }

  protected void failRakeTaskBuild(@NotNull final MyBuildFailureException e) throws RunBuildException {
    getBuildLogger().buildFailureDescription(e.getTitle());

    throw new RunBuildException(e.getMessage());
  }

  @Override
  protected boolean shouldDumpOutputLinesOnError() {
    return false;
  }

  @Override
  protected void onOutput(final String lineWithoutLF, final Key lastOutputKey) {
    super.onOutput(lineWithoutLF, lastOutputKey);
    final String s = StringUtil.stripNewLine(lineWithoutLF);

    if (lastOutputKey == ProcessOutputTypes.STDERR) {
//      getBuildLogger().error(s);
      getBuildLogger().warning(s);
    }
    else {
      //TODO: think about SystemOutput
      getBuildLogger().message(s);
    }
  }

  @Override
  protected void processWillBeTerminated(final Map<String, String> runParameters,
                                         final ProcessEvent processEvent,
                                         final boolean b) {
    getBuildLogger().flush();
    super.processWillBeTerminated(runParameters, processEvent, b);
  }

  @Override
  protected void processTerminated(final RunEnvironment runEnvironment,
                                   final boolean isFailed) {
    getBuildLogger().flush();
    super.processTerminated(runEnvironment, isFailed);

    // Remove tmp files
    for (File file : myFilesToDelete) {
      jetbrains.buildServer.util.FileUtil.delete(file);
    }
    myFilesToDelete.clear();
  }

  private void addTestRunnerPatchFiles(final Map<String, String> runParams,
                                       final Map<String, String> buildParams,
                                       final HashMap<String, String> envMap)
      throws MyBuildFailureException, RunBuildException {

    final String patchedRubySDKFilesRoot = RubyProjectSourcesUtil.getPatchedRubySDKFilesRoot();
    // adds out patch to loadpath
    envMap.put(RUBYLIB_ENVIRONMENT_VARIABLE,
        OSUtil.appendToRUBYLIBEnvVariable(patchedRubySDKFilesRoot));
    // due to patching loadpath we replace original autorunner but it is used buy our tests runner
    envMap.put(ORIGINAL_SDK_AUTORUNNER_PATH_KEY,
        RubySDKUtil.getSDKTestUnitAutoRunnerScriptPath(runParams, buildParams));
    envMap.put(ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY,
        RubySDKUtil.getSDKTestUnitTestRunnerMediatorScriptPath(runParams, buildParams));
  }

  private void addCmdlineArguments(@NotNull final GeneralCommandLine cmdLine, @NotNull final String argsString) {
    final List<String> stringList = StringUtil.splitHonorQuotes(argsString, ' ');
    for (String arg : stringList) {
      cmdLine.addParameter(stripDoubleQuoteAroundValue(arg));
    }
  }

  private String stripDoubleQuoteAroundValue(@NotNull final String str) {
    String text = str;
    if (StringUtil.startsWithChar(text, '\"')) {
      text = text.substring(1);
    }
    if (StringUtil.endsWithChar(text, '\"')) {
      text = text.substring(0, text.length() - 1);
    }
    return text;
  }
 
  @Nullable
  private File getBuildFile(Map<String, String> runParameters) throws IOException, RunBuildException {
    final File buildFile;
    if (BuildFileRunnerUtil.isCustomBuildFileUsed(runParameters)) {
      buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
      if (buildFile != null) {
        myFilesToDelete.add(buildFile);
      }
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
