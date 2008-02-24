/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import static jetbrains.buildServer.runner.BuildFileRunnerConstants.BUILD_FILE_PATH_KEY;
import jetbrains.buildServer.runner.BuildFileRunnerUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 03.06.2007
 */
public class RakeTasksRunner extends RakeRunnerBase {
    protected static final Logger LOG = Logger.getLogger(RakeTasksRunner.class.getName());

    private final List<String> myStdOutMessages = new LinkedList<String>();
    private final List<String> myStdErrMessages = new LinkedList<String>();

    private final Set<File> myFilesToDelete = new HashSet<File>();
    private final String RSPEC_RUNNER_OPTIONS_REQUIRE = "--require Spec::Runner::Formatter::TeamcityFormatter:matrix";
    private final String RSPEC_RUNNERR_OPTIONS_FORMATTER = "--format spec/runner/formatter/teamcity/formatter";

    @NonNls
    public String getType() {
        return RakeRunnerConstants.RUNNER_TYPE;
    }

    protected void modifyBuildEnvironment(final Map<String, String> environment,
                                          final Map<String, String> runParameters,
                                          final Map<String, String> buildParameters,
                                          final File tempDir) {
        super.modifyBuildEnvironment(environment, runParameters, buildParameters, tempDir);

        environment.put(AgentRuntimeProperties.BUILD_ID,
                        runParameters.get(AgentRuntimeProperties.BUILD_ID));
        environment.put(AgentRuntimeProperties.OWN_PORT,
                        runParameters.get(AgentRuntimeProperties.OWN_PORT));
    }

    protected void buildCommandLine(@NotNull final GeneralCommandLine cmd,
                                    @NotNull final File soourcesRootDir,
                                    @NotNull final Map<String, String> runParams,
                                    @NotNull final Map<String, String> buildParams)
            throws IOException, RunBuildException {

        final boolean inDebugMode = ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.DEBUG_PROPERTY);

        final File buildFile = getBuildFile(runParams);

        final String patchedRubySDKFilesRoot = RubySourcesUtil.getPatchedRubySDKFilesRoot();
        try {
            // Prerequisites
            if (!RubySDKUtil.isGemInstalledInSDK(RubySDKUtil.GEM_BUILDER_NAME, null, true, runParams, buildParams)) {
                final String msg = "Unable to find 'builder' gem for Ruby SDK with interpreter: '"
                                    + ExternalParamsUtil.getRubyInterpreterPath(runParams, buildParams)
                                    + "'. This gem is mandatory for TeamCity Rake Runner. Please install 'builder' gem for this Ruby SDK.";

                throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
            }

            // Special rake runner Environment properties
            final HashMap<String, String> envMap = new HashMap<String, String>();
            envMap.put(RUBYLIB_ENVIRONMENT_VARIABLE, OSUtil.appendToRUBYLIBEnvVariable(patchedRubySDKFilesRoot));
            envMap.put(ORIGINAL_SDK_AUTORUNNER_PATH_KEY, RubySDKUtil.getSDKTestUnitAutoRunnerScriptPath(runParams, buildParams));
            if (ExternalParamsUtil.isParameterEnabled(runParams, SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED)) {
                envMap.put(RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY, Boolean.TRUE.toString());
            }
            cmd.setEnvParams(envMap);

            // Ruby interpreter
            cmd.setExePath(ExternalParamsUtil.getRubyInterpreterPath(runParams, buildParams));

            // Rake runner script
            cmd.addParameter(RubySourcesUtil.getRakeRunnerPath());

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

            //Test::Unit TESTOPTS
            final String testOpts = runParams.get(SERVER_UI_RAKE_TEST_UNIT_TESTOPTS_PROPERTY);
            if (!TextUtil.isEmptyOrWhitespaced(testOpts)) {
                final String trimedTestOpts = testOpts.trim();
                cmd.addParameter("\"" + RAKE_TEST_UNIT_TESTOPTS_PARAM_NAME + "=" + trimedTestOpts + "\"");
            }

            final String specRunnerInitString = RSPEC_RUNNER_OPTIONS_REQUIRE + " " + RSPEC_RUNNERR_OPTIONS_FORMATTER;
            String specOpts = runParams.get(SERVER_UI_RSPEC_SPEC_OPTS_PROPERTY);
            if (TextUtil.isEmpty(specOpts)) {
                specOpts = specRunnerInitString;
            } else {
                specOpts = specOpts.trim() + " " + specRunnerInitString;
            }
            cmd.addParameter("\"" + RAKE__RSPEC_SPEC_OPTS_PARAM_NAME + "=" + specOpts.trim() + "\"");

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

    private void addCmdlineArguments(GeneralCommandLine cmdLine, String argsString) {
        final StringTokenizer st = new StringTokenizer(argsString);
        while (st.hasMoreTokens()) {
            cmdLine.addParameter(st.nextToken());
        }
    }

    protected void onTextAvailable(final Map<String, String> runParameters,
                                   final ProcessEvent processEvent, final Key outputType) {
        super.onTextAvailable(runParameters, processEvent, outputType);

        final String text = TextUtil.removeNewLine(processEvent.getText());
        if (outputType == ProcessOutputTypes.SYSTEM) {
            getBuildLogger().message(text);
        } else if (outputType == ProcessOutputTypes.STDOUT) {
            synchronized (myStdOutMessages) {
                myStdOutMessages.add(processEvent.getText());
            }
        } else {
            synchronized (myStdErrMessages) {
                myStdErrMessages.add(processEvent.getText());
            }
        }
    }

    protected boolean shouldDumpOutputLinesOnError() {
        return false;
    }

    protected void processTerminated(RunEnvironment runEnvironment, final boolean isFailed) {
        dumpOutputMessages(true);
        dumpOutputMessages(false);

        for (File file : myFilesToDelete) {
          jetbrains.buildServer.util.FileUtil.delete(file);
        }
        myFilesToDelete.clear();
    }

    private void dumpOutputMessages(final boolean dumpStdOut) {
        final List<String> messages;
        final String outputType;
        if (dumpStdOut) {
            messages = myStdOutMessages;
            outputType = "stdout";
        } else {
            messages = myStdErrMessages;
            outputType = "stderr";
        }

        synchronized (messages) {
            if (messages.size() > 0) {
                final StringBuilder sb = new StringBuilder();
                sb.append(getType()).append(" uncaptured ").append(outputType).append(":\n");
                for (String s : messages) {
                    sb.append(s);
                }
                if (dumpStdOut) {
                    getBuildLogger().message(sb.toString());
                } else {
                    getBuildLogger().warning(sb.toString());
                }
                getBuildLogger().flush();
            }
            messages.clear();
        }
    }

    @Nullable
    private File getBuildFile(Map<String, String> runParameters) throws IOException, RunBuildException {
        final File buildFile;
        if (BuildFileRunnerUtil.isCustomBuildFileUsed(runParameters)) {
            buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
        } else {
            final String buildFilePath = runParameters.get(BUILD_FILE_PATH_KEY);
            if (PropertiesUtil.isEmptyOrNull(buildFilePath)) {
                //use rake defaults
                buildFile = null;
            } else {
                buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
            }
        }
        if (buildFile != null) {
            myFilesToDelete.add(buildFile);
        }
        return buildFile;
    }

    public void failRakeTaskBuild(@NotNull final MyBuildFailureException e) throws RunBuildException {
        getBuildLogger().buildFailureDescription(e.getTitle());

        throw new RunBuildException(e.getMessage());
    }

    public static class MyBuildFailureException extends Exception {
        private final String msg;
        private final String title;

        public MyBuildFailureException(@NotNull final String msg,
                                       @NotNull final String title) {
            this.msg = msg;
            this.title = title;
        }

        public String getMessage() {
            return msg;
        }

        public String getTitle() {
            return title;
        }
    }
}
