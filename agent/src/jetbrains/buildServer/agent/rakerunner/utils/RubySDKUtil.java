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

package jetbrains.buildServer.agent.rakerunner.utils;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman.Chernyatchik
 * @date: 30.01.2008
 */
public class RubySDKUtil {
    @NonNls
    public static final String AUTORUNNER_SCRIPT_PATH = "test/unit/autorunner.rb";

    @NonNls
    private static final String GET_LOAD_PATH_SCRIPT =  "puts $LOAD_PATH";

    @NotNull
    public static String getSDKTestUnitAutoRunnerScriptPath(@NotNull final Map<String, String> runParameters,
                                                            @NotNull final Map<String, String> buildParameters)
            throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

        final String scriptSource = GET_LOAD_PATH_SCRIPT;

        final String rubyExecutable = ExternalParamsUtil.getRubyInterpreterPath(runParameters, buildParameters);
        final Runner.Output result =  Runner.runScriptFromSource(rubyExecutable, new String[0],
                                                                 scriptSource, new String[0]);
        final String loadPaths[] = TextUtil.splitByLines(result.getStdout());
        for (String path : loadPaths) {
            final String fullPath = toSystemIndependentName(path + File.separatorChar + AUTORUNNER_SCRIPT_PATH);
            if (FileUtil.checkIfExists(fullPath)) {
                return fullPath;
            }
        }

        // Error
        final String msg = "File '" + AUTORUNNER_SCRIPT_PATH + "' wasn't found in $LOAD_PATH of Ruby SDK with interpreter: '" + rubyExecutable + "'";
        throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }
}
