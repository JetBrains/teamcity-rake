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

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import java.io.File;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class RubySDKUtil {
  @NonNls
  public static final String AUTORUNNER_SCRIPT_PATH = "test/unit/autorunner.rb";
  @NonNls
  public static final String TESTRUNNERMEDIATOR_SCRIPT_PATH = "test/unit/ui/testrunnermediator.rb";

  @NonNls
  private static final String GET_LOAD_PATH_SCRIPT =  "puts $LOAD_PATH";

  @NotNull
  public static String getSDKTestUnitAutoRunnerScriptPath(@NotNull final Map<String, String> runParameters,
                                                          @NotNull final Map<String, String> buildParameters)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    return findSdkScript(runParameters, buildParameters, AUTORUNNER_SCRIPT_PATH);
  }

  @NotNull
  public static String getSDKTestUnitTestRunnerMediatorScriptPath(@NotNull final Map<String, String> runParameters,
                                                                  @NotNull final Map<String, String> buildParameters)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    return findSdkScript(runParameters, buildParameters, TESTRUNNERMEDIATOR_SCRIPT_PATH);
  }

  private static RubyScriptRunner.Output executeScriptFromSource(@NotNull final Map<String, String> runParameters,
                                                       @NotNull final Map<String, String> buildParameters, String scriptSource)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    final String rubyExecutable =
        ConfigurationParamsUtil.getRubyInterpreterPath(runParameters, buildParameters);

    return RubyScriptRunner.runScriptFromSource(rubyExecutable, new String[0], scriptSource, new String[0]);
  }

  /**
   * Finds script with given relative path in SDK
   * @param runParameters Run params
   * @param buildParameters Build params
   * @param scriptPath Path of given script
   * @return Full path of given script
   * @throws RakeTasksRunner.MyBuildFailureException If script will not be found
   * @throws RunBuildException Other error
   */
  private static String findSdkScript(final Map<String, String> runParameters,
                                      final Map<String, String> buildParameters,
                                      final String scriptPath) throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    final String scriptSource = GET_LOAD_PATH_SCRIPT;
    final RubyScriptRunner.Output result = executeScriptFromSource(runParameters, buildParameters, scriptSource);
    final String loadPaths[] = TextUtil.splitByLines(result.getStdout());
    for (String path : loadPaths) {
      final String fullPath = toSystemIndependentName(path + File.separatorChar + scriptPath);
      if (FileUtil.checkIfExists(fullPath)) {
        return fullPath;
      }
    }

    // file wasn't found
    if (!TextUtil.isEmpty(result.getStderr())) {
      throw new RakeTasksRunner.MyBuildFailureException(result.getStdout() + "\n" + result.getStderr(),
                                                        RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }

    for (String path : loadPaths) {
      if (path.contains("JAVA_HOME")) {
        throw new RakeTasksRunner.MyBuildFailureException(result.getStdout(),
                                                          RakeRunnerBundle.RUNNER_ERROR_TITLE_JRUBY_PROBLEMS_IN_CONF_ON_AGENT);
      }
    }

    // Error
    final String msg = "File '" + scriptPath + "' wasn't found in $LOAD_PATH of Ruby SDK with interpreter: '" + ConfigurationParamsUtil.getRubyInterpreterPath(runParameters, buildParameters) + "'";
    throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
  }
}
