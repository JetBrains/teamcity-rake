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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman.Chernyatchik
 * @date: 22.12.2007
 */
public class ExternalParamsUtil implements RakeRunnerConstants {
    private static final Logger LOG = Logger.getLogger(ExternalParamsUtil.class);


//    private static PropertyFinder RUBY_INTERPRETER_FINDER =
//            new PropertyFinder(TARGET_RUBY_INTERPRETER,
//                               ENV_VARIABLE_RUBY_INTERPRETER,
//                               SYSTEM_PROPERTY_RUBY_INTERPRETER);


    @NotNull
    public static String getRubyInterpreterPath(final Map<String, String> runParameters,
                                                final Map<String, String> buildParameters)
            throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

        final String rubyInterpreterPath =
//                RUBY_INTERPRETER_FINDER.getPropertyValue(runParameters, buildParameters);
                runParameters.get(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER);
//TODO "PATH" support
        if (rubyInterpreterPath == null) {
            final String msg = "Unable to find ruby home. Check property '";
//TODO
//                    + RakeRunnerConstants.TARGET_RUBY_INTERPRETER
//                    + "', enviroment variable '"
//                    + RakeRunnerConstants.ENV_VARIABLE_RUBY_INTERPRETER
//                    + "' or system property '"
//                    + RakeRunnerConstants.SYSTEM_PROPERTY_RUBY_INTERPRETER + "'";

            throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
        }

        final File rubyInterpreter = new File(rubyInterpreterPath);
        try {
            if (rubyInterpreter.exists() && rubyInterpreter.isFile()) {
                return rubyInterpreterPath;
            }
            final String msg = "Ruby interpreter (" + rubyInterpreterPath + ") doesn't exist or isn't a file.";
            throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
        } catch (Exception e) {
            //unknown error
            throw new RunBuildException(e.getMessage(), e);
        }
    }

    public static boolean isParameterEnabled(final Map<String, String> runParameters,
                                             final String key) {
       return runParameters.containsKey(key)
               && runParameters.get(key).equals(Boolean.TRUE.toString());
     }

}
