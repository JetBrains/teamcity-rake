/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import static com.intellij.openapi.util.io.FileUtil.toSystemDependentName;

/**
 * @author Roman.Chernyatchik
 */
public class RubyScriptRunner {
  private static final Logger LOG = Logger.getInstance(RubyScriptRunner.class.getName());

  /**
   * Returns out after scriptSource run.
   *
   * @param rubyExe      Path to ruby executable
   * @param scriptSource script source to tun
   * @param rubyArgs     ruby Arguments
   * @param scriptArgs   script arguments
   * @return Out object
   */
  @NotNull
  public static Output runScriptFromSource(@NotNull final RubySdk sdk,
                                           @NotNull final String[] rubyArgs,
                                           @NotNull final String scriptSource,
                                           @NotNull final String[] scriptArgs,
                                           @Nullable final Map<String, String> buildConfEnvironment) {
    Output result = null;
    File scriptFile = null;
    try {
      // Writing source to the temp file
      scriptFile = File.createTempFile("script", ".rb");
      PrintStream out = new PrintStream(scriptFile);
      out.print(scriptSource);
      out.close();

      //Args
      final String[] args = new String[2 + rubyArgs.length + scriptArgs.length];
      args[0] = sdk.getInterpreterPath();
      System.arraycopy(rubyArgs, 0, args, 1, rubyArgs.length);
      args[rubyArgs.length + 1] = scriptFile.getPath();
      System.arraycopy(scriptArgs, 0, args, rubyArgs.length + 2, scriptArgs.length);

      // Env
      final HashMap<String, String> processEnv = new HashMap<String, String>(buildConfEnvironment);
      RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, processEnv);

      //Result
      result = RubyScriptRunner.runInPath(null, processEnv, args);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      if (scriptFile != null && scriptFile.exists()) {
        scriptFile.delete();
      }
    }

    //noinspection ConstantConditions
    return result;
  }

  /**
   * Returns output after execution.
   *
   * @param workingDir working directory
   * @param command    Command to execute @return Output object
   * @return Output
   */
  @NotNull
  public static Output runInPath(@Nullable final String workingDir,
                                 @Nullable final Map<String, String> environment,
                                 @NotNull final String... command) {
// executing
    final StringBuilder out = new StringBuilder();
    final StringBuilder err = new StringBuilder();
    Process process = createProcess(workingDir, environment, command);
    if (process != null) {
      ProcessHandler osProcessHandler = new OSProcessHandler(process, TextUtil.concat(command)) {
        private final Charset DEFAULT_SYSTEM_CHARSET = CharsetToolkit.getDefaultSystemCharset();

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
   * @param command    add command line
   * @param workingDir add working directory or null, if no special needed
   * @return add
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
      if (!isUnitTest()) {
        LOG.error(e.getMessage(), e);
      } else {
        LOG.warn(e.getMessage(), e);
      }
    }
    return process;
  }

  private static boolean isUnitTest() {
    //This is jetbrains.buildServer.log.LogInitializer.isUnitTest():
    //this class is shared with RubyMine sources, let's avoaid adding TC dependency here
    return "yes".equals(System.getProperty("jetbrains.unit.test"));
  }

  /**
   * Creates process builder and setups it's commandLine, working directory, enviroment variables
   *
   * @param workingDir         Process working dir
   * @param executablePath     Path to executable file
   * @param arguments          Process commandLine
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
    final Map<String, String> envParams = new HashMap<String, String>();
    envParams.putAll(environment);
    cmdLine.setEnvParams(envParams);

    return cmdLine;
  }


  public static class Output {
    private String stdout;
    private String stderr;

    public Output(String stdout, String stderr) {
      this.stdout = stdout;
      this.stderr = stderr;
    }

    public String getStdout() {
      return stdout;
    }

    public String getStderr() {
      return stderr;
    }
  }

  public static class OutputListener extends ProcessAdapter {
    private final StringBuilder out;
    private final StringBuilder err;

    public OutputListener(@NotNull final StringBuilder out, @NotNull final StringBuilder err) {
      this.out = out;
      this.err = err;
    }

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
