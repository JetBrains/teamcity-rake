/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.CharsetToolkit;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.log.LogInitializer;
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
   * @return Output process output
   */
  @NotNull
  public static Output run(@Nullable final String workingDir,
                           @Nullable final Map<String, String> environment,
                           @NotNull final String... command) {
    // executing
    final StringBuilder out = new StringBuilder();
    final StringBuilder err = new StringBuilder();
    Process process = createProcess(workingDir, environment, command);
    if (process != null) {
      ProcessHandler osProcessHandler = new OSProcessHandler(process, TextUtil.concat(command)) {
        private final Charset DEFAULT_SYSTEM_CHARSET = CharsetToolkit.getDefaultSystemCharset();

        @Override
        public Charset getCharset() {
          return DEFAULT_SYSTEM_CHARSET;
        }
      };
      osProcessHandler.addProcessListener(new OutputListener(out, err));
      osProcessHandler.startNotify();
      osProcessHandler.waitFor();
    }
    return new Output(out.toString(), err.toString());
  }

  /**
   * Creates add by command and working directory
   *
   * @param workingDir  working directory or null, if no special needed
   * @param environment additional environment for process
   * @param command     command line
   * @return see above
   */
  @Nullable
  public static Process createProcess(@Nullable final String workingDir,
                                      @Nullable final Map<String, String> environment,
                                      @NotNull final String... command) {
    Process process = null;

    final String[] arguments;
    if (command.length > 1) {
      arguments = new String[command.length - 1];
      System.arraycopy(command, 1, arguments, 0, command.length - 1);
    } else {
      arguments = new String[0];
    }

    final GeneralCommandLine cmdLine = createAndSetupCmdLine(workingDir, environment, command[0], arguments);
    try {
      process = cmdLine.createProcess();
    } catch (Exception e) {
      if (!LogInitializer.isUnitTest()) {
        LOG.error(e.getMessage(), e);
      } else {
        LOG.warn(e.getMessage(), e);
      }
    }
    return process;
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
  public static GeneralCommandLine createAndSetupCmdLine(@Nullable final String workingDir,
                                                         @Nullable final Map<String, String> environment,
                                                         @NotNull final String executablePath,
                                                         @NotNull final String... arguments) {
    final GeneralCommandLine cmdLine = new GeneralCommandLine();

    cmdLine.setExePath(toSystemDependentName(executablePath));
    if (workingDir != null) {
      cmdLine.setWorkDirectory(toSystemDependentName(workingDir));
    }
    cmdLine.addParameters(arguments);

    // set env params
    if (environment != null) {
      final Map<String, String> envParams = new HashMap<String, String>();
      envParams.putAll(environment);
      cmdLine.setEnvParams(envParams);
    }

    return cmdLine;
  }

  public static class Output {
    @NotNull
    private final String myStdout;
    @NotNull
    private final String myStderr;

    public Output(@NotNull final String stdout, @NotNull final String stderr) {
      this.myStdout = stdout;
      this.myStderr = stderr;
    }

    @NotNull
    public String getStdout() {
      return myStdout;
    }

    @NotNull
    public String getStderr() {
      return myStderr;
    }
  }

  public static class OutputListener extends ProcessAdapter {
    private final StringBuilder out;
    private final StringBuilder err;

    public OutputListener(@NotNull final StringBuilder out, @NotNull final StringBuilder err) {
      this.out = out;
      this.err = err;
    }

    @Override
    public void onTextAvailable(final ProcessEvent event, final Key outputType) {
      if (outputType == ProcessOutputTypes.STDOUT) {
        out.append(event.getText());
      }
      if (outputType == ProcessOutputTypes.STDERR) {
        err.append(event.getText());
      }
    }
  }
}
