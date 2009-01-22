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

package jetbrains.buildServer.agent.rakerunner.utils;

import java.io.File;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class ConfigurationParamsUtil implements RakeRunnerConstants {

  @NotNull
  public static String getRubyInterpreterPath(final Map<String, String> runParameters,
                                              final Map<String, String> buildParameters)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    final String rubyInterpreterPath;

    // Check if path to ruby interpreter was explicitly set
    // and calculate corresponding interpreter path
    final String uiRubyInterpreterPath = runParameters.get(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER);
    if (PropertiesUtil.isEmptyOrNull(uiRubyInterpreterPath)) {
      // find in $PATH
      final String path = OSUtil.findRubyInterpreterInPATH(buildParameters);
      if (path != null) {
        rubyInterpreterPath = path;
      } else {
        final String msg = "Unable to find Ruby interpreter in PATH.";
        throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      }
    } else {
      // get from UI
      rubyInterpreterPath = uiRubyInterpreterPath;
    }

    // Check that interpreter file exists
    final File rubyInterpreter = new File(rubyInterpreterPath);
    try {

      if (rubyInterpreter.exists() && rubyInterpreter.isFile()) {
        return rubyInterpreterPath;
      }
      final String msg = "Ruby interpreter '" + rubyInterpreterPath + "' doesn't exist or isn't a file.";
      throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    } catch (SecurityException e) {
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
