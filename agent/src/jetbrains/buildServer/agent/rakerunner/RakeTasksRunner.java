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
import com.intellij.openapi.util.Key;
import com.intellij.util.containers.HashMap;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.PropertiesUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 03.06.2007
 */
public class RakeTasksRunner extends RakeRunnerBase {
    protected static final Logger LOG = Logger.getLogger(RakeTasksRunner.class.getName());

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

        final String patchedRubySDKFilesRoot = RubySourcesUtil.getPatchedRubySDKFilesRoot();

        try {
            // Special rake runner Environment properties
            final HashMap<String, String> envMap = new HashMap<String, String>();
            envMap.put(RakeRunnerConstants.RUBYLIB_ENVIRONMENT_VARIABLE,
                    OSUtil.appendToRUBYLIBEnvVariable(patchedRubySDKFilesRoot));
            envMap.put(RakeRunnerConstants.ORIGINAL_SDK_AUTORUNNER_PATH_KEY,
                    RubySDKUtil.getSDKTestUnitAutoRunnerScriptPath(runParams, buildParams));
            cmd.setEnvParams(envMap);

            // Ruby interpreter
            cmd.setExePath(ExternalParamsUtil.getRubyInterpreterPath(runParams, buildParams));

            // Rake runner script
            cmd.addParameter(RubySourcesUtil.getRakeRunnerPath());

            // Rake options
            if (ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_RAKE_OPTION_TRACE_PROPERTY)) {
                cmd.addParameter(RakeRunnerConstants.AGENT_CMD_LINE_RAKE_OPTION_TRACE_FLAG);
            }
            if (ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_RAKE_OPTION_QUIET_PROPERTY)) {
                cmd.addParameter(RakeRunnerConstants.AGENT_CMD_LINE_RAKE_OPTION_QUIET_FLAG);
            }
            if (ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_RAKE_OPTION_DRYRUN_PROPERTY)) {
                cmd.addParameter(RakeRunnerConstants.AGENT_CMD_LINE_RAKE_OPTION_DRYRUN_FLAG);
            }

            // Task name
            final String task_name = runParams.get(RakeRunnerConstants.SERVER_UI_RAKE_TASK_PROPERTY);
            if (PropertiesUtil.isEmptyOrNull(task_name)) {
                cmd.addParameter(RakeRunnerConstants.DEFAULT_RAKE_TASK_NAME);
            } else {
                cmd.addParameter(task_name);
            }

//        if (user_params != null) {
//            final StringTokenizer st = new StringTokenizer(user_params);
//            while (st.hasMoreTokens()) {
//                cmd.addParameter(st.nextToken());
//            }
//        }  else {
//            throw new RunBuildException("Specify Rake task name in runner configuration settings.");
//        }
            //TODO if user allow
            // cmd.addParameter("TESTOPTS=\\\"C:/home/teamcity/rubyteamcity/rakerunner/src/teamcity_testrunner.rb\\\" --runner=teamcity");

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

    protected void onTextAvailable(final Map<String, String> runParameters,
                                   final ProcessEvent processEvent, final Key key) {
        super.onTextAvailable(runParameters, processEvent, key);
        //TODO script hide run params
        final String text = TextUtil.removeNewLine(processEvent.getText());
        getBuildLogger().message("{AGENT OUTPUT}: " + text);
    }

    public void failRakeTaskBuild(@NotNull final MyBuildFailureException e) throws RunBuildException {
        getBuildLogger().error(e.getMessage());
        getBuildLogger().buildFailureDescription(e.getTitle());

        throw new RunBuildException(e.getTitle());
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
