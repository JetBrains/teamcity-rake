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

import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.PropertyFinder;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.apache.log4j.Logger;

import java.util.Map;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman.Chernyatchik
 * @date: 22.12.2007
 */
public class ExternalParamsUtil implements RakeRunnerConstants {
    private static final Logger LOG = Logger.getLogger(ExternalParamsUtil.class);


    private static PropertyFinder RUBY_INTERPRETER_FINDER =
            new PropertyFinder(TARGET_RUBY_INTERPRETER,
                               ENV_VARIABLE_RUBY_INTERPRETER,
                               SYSTEM_PROPERTY_RUBY_INTERPRETER);


    @Nullable
    public static String getAgentSystemOrEnvProperty(@NotNull final String systemPropertyName,
                                                     @NotNull final String envPropertyName,
                                                     @NotNull final BuildAgentConfiguration config) {
       return getAgentSystemOrEnvProperty(systemPropertyName, envPropertyName, config, false);
    }
    @Nullable
    private static String getAgentSystemOrEnvProperty(@NotNull final String systemPropertyName,
                                                     @NotNull final String envPropertyName,
                                                     @NotNull final BuildAgentConfiguration config,
                                                     final boolean logResults) {

        //TODO which order is right? What about "system." and "env." prefixes?

        //system
        final Object systemValue = config.getCustomProperties().get(systemPropertyName);
        if (systemValue instanceof String) {
            return (String) systemValue;
        }

        //env
        final String envValue = config.getEnv(envPropertyName);   //System.env
        if (envValue != null) {
            return envValue;
        }
        return null;
    }

    public static boolean isAgentPropertyDefined(@NotNull final String systemPropertyName,
                                                 @NotNull final  String envPropertyName,
                                                 @NotNull final BuildAgentConfiguration config) {
        return getAgentSystemOrEnvProperty(systemPropertyName, envPropertyName, config) != null;
    }

    @Nullable
    public static String getRubyInterpreterPath(@NotNull final String systemPropertyName,
                                                @NotNull final String envPropertyName,
                                                @NotNull final BuildAgentConfiguration config) {
       return getAgentSystemOrEnvProperty(systemPropertyName, envPropertyName, config, true);
    }


    @NotNull
    public static String getRubyInterpreterPath(final Map<String, String> runParameters,
                                                final Map<String, String> buildParameters)
            throws RunBuildException {
        //TODO refactor with corresponding method

        final String rubyInterpreterPath =
                RUBY_INTERPRETER_FINDER.getPropertyValue(runParameters, buildParameters);

        if (rubyInterpreterPath == null) {
            throw new RunBuildException("Unable to find ruby home. Check property '"
                    + RakeRunnerConstants.TARGET_RUBY_INTERPRETER
                    + "', enviroment variable '"
                    + RakeRunnerConstants.ENV_VARIABLE_RUBY_INTERPRETER
                    + "' or system property '"
                    + RakeRunnerConstants.SYSTEM_PROPERTY_RUBY_INTERPRETER + "'");
        }

          final File rubyInterpreter = new File(rubyInterpreterPath);
          try {
              if (rubyInterpreter.exists() && rubyInterpreter.isFile()) {
                  return rubyInterpreterPath;
              }
              throw new RunBuildException("Ruby interpreter ("+ rubyInterpreterPath + ") doesn't exist or isn't a file.");
          } catch (Exception e) {
              throw new RunBuildException(e);
          }
    }

    public static boolean isParameterEnabled(final Map<String, String> runParameters,
                                             final String key) {
       return runParameters.containsKey(key)
               && runParameters.get(key).equals(Boolean.TRUE.toString());
     }

}
