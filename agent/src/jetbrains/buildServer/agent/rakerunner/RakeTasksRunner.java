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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.rakerunner.utils.ExternalParamsUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RubySourcesUtil;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import jetbrains.buildServer.agent.rakerunner.RakeRunnerBase;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
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

    public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
        final BuildAgentSystemInfo systemInfo = agentConfiguration.getSystemInfo();
        if (!systemInfo.isWindows() && !systemInfo.isMac()
                && !systemInfo.isUnix()) {
            LOG.info(getType() + " runner can works only under Unix, Linux, Windows and MacOS");
            return false;
        }

        // Interpreter path
        if (!ExternalParamsUtil.isAgentPropertyDefined(SYSTEM_PROPERTY_RUBY_INTERPRETER,
                                                                 ENV_VARIABLE_RUBY_INTERPRETER,
                                                                 agentConfiguration)) {
            LOG.info(getRunnerNotRegisteredMessage() + " "
                    + SYSTEM_PROPERTY_RUBY_INTERPRETER + " system property or "
                    + ENV_VARIABLE_RUBY_INTERPRETER + " environment variable is required");
            return false;
        }

        // TODO Rake gem > 0.7.3...
        // TODO Builder gem...
        return true;
    }

    protected void buildCommandLine(@NotNull final GeneralCommandLine cmd,
                                    @NotNull final File workingDir,
                                    @NotNull final Map<String, String> runParams,
                                    @NotNull final Map<String, String> buildParams)
            throws IOException, RunBuildException {

        final boolean inDebugMode = ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.DEBUG_PROPERTY);

        // Ruby interpreter
        cmd.setExePath(ExternalParamsUtil.getRubyInterpreterPath(runParams, buildParams));

        // Working directory
        final String workDir = runParams.get(RakeRunnerConstants.SERVER_UI_WORK_DIR_PROPERTY);
        if (!PropertiesUtil.isEmptyOrNull(workDir)) {
            cmd.setWorkDirectory(workDir);
        }

        // Rake runner script
        cmd.addParameter(RubySourcesUtil.getRakeRunnerPath());

//TODO
//        cmd.addParameter("-e");
//        cmd.addParameter("\"require(\\\"C:/home/teamcity/rubyteamcity/rakerunner/src/arguments\\\");STDOUT.sync=true;STDERR.sync=true;load($0=ARGV.shift)\"");

        // TeamCity connection params
        cmd.addParameter(runParams.get(AgentRuntimeProperties.BUILD_ID)); //build
        cmd.addParameter(runParams.get(AgentRuntimeProperties.OWN_PORT)); //port

        // Rake options
        if (ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_RAKE_OPTION_TRACE_PROPERTY)) {
            cmd.addParameter("--trace");
        }

        if (ExternalParamsUtil.isParameterEnabled(runParams, RakeRunnerConstants.SERVER_UI_RAKE_OPTION_QUITE_PROPERTY)) {
            cmd.addParameter("--quite");
        }

        // Task name
        final String task_name = runParams.get(RakeRunnerConstants.SERVER_UI_RAKE_TASK_PROPERTY);
        if (PropertiesUtil.isEmptyOrNull(task_name)) {
            throw new RunBuildException("Specify Rake task name in runner configuration settings.");
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
    }

    protected void onTextAvailable(final Map<String, String> runParameters,
                                   final ProcessEvent processEvent, final Key key) {
        super.onTextAvailable(runParameters, processEvent, key);
        //TODO script hide run params
        final String text = TextUtil.removeNewLine(processEvent.getText());
        getBuildLogger().message("{AGENT TEXT AVAILABLE}: " + text);
    }

}
