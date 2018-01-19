/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.io.FileUtil.toSystemDependentName;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public class RunnerUtil {
  private static final Logger LOG = Logger.getInstance(RunnerUtil.class.getName());

  /**
   * Sync process execution.
   *
   * @param workingDir  working directory or null, if no special needed
   * @param environment additional environment for process
   * @param command     Command to execute
   * @return ExitResult process execution result
   */
  @NotNull
  public static ExecResult run(@Nullable final String workingDir,
                               @Nullable final Map<String, String> environment,
                               @NotNull final String... command) {
    // executing
    if (LOG.isDebugEnabled()) {
      LOG.debug("Running " + Arrays.asList(command) + " in " + workingDir + " with env " + environment);
    }
    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(createCommandLine(workingDir, environment, command), null);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Result is " + result);
    }
    return result;
  }

  /**
   * Creates process builder and setups it's commandLine, working directory, environment variables
   *
   * @param workingDir     Process working dir
   * @param environment    additional environment for process
   * @param executablePath Path to executable file
   * @param arguments      Process commandLine
   * @return process builder
   */
  @NotNull
  public static GeneralCommandLine createCommandLine(@Nullable final String workingDir,
                                                     @Nullable final Map<String, String> environment,
                                                     @NotNull final String... command) {
    if (command.length == 0) {
      throw new IllegalArgumentException("At least executable path in 'command' argument required");
    }
    if (StringUtil.isEmptyOrSpaces(command[0])) {
      throw new IllegalArgumentException("First string in 'command' argument (executable path) must not be empty");
    }

    final GeneralCommandLine cl = new GeneralCommandLine();

    cl.setExePath(toSystemDependentName(command[0]));
    if (workingDir != null) {
      cl.setWorkDirectory(toSystemDependentName(workingDir));
    }
    for (int i = 1; i < command.length; i++) {
      cl.addParameter(command[i]);
    }

    cl.setEnvParams(environment != null ? new HashMap<String, String>(environment) : null);

    return cl;
  }

}
