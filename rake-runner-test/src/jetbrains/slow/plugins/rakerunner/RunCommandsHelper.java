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

package jetbrains.slow.plugins.rakerunner;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Vladislav.Rassokhin
 */
public class RunCommandsHelper {


  public static long runExecutable(@NotNull final Logger log, @NotNull final String command, @NotNull final File workingDirectory, String... args) {
    final GeneralCommandLine cl = new GeneralCommandLine();
    cl.setExePath(command);
    cl.setWorkingDirectory(workingDirectory);
    cl.addParameters(args);
    log.debug("Running " + command + " with " + Arrays.toString(args) + " at " + workingDirectory.getAbsolutePath());
    Long start = System.currentTimeMillis();
    final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cl, null);
    Long duration = System.currentTimeMillis() - start;
    final Throwable e = result.getException();
    if (e != null) {
      throw new RuntimeException("Failed to run " + command, e);
    }
    if (result.getExitCode() != 0) {
      log.error(result.toString());
      throw new RuntimeException("Non zero exit code of " + command + " Actual code is " + result.getExitCode());
    } else if (log.isDebugEnabled()) {
      log.debug(result.toString());
    }
    log.info("Successfully in " + duration + "msec " + command + " with " + Arrays.toString(args) + " at " + workingDirectory.getAbsolutePath());
    return duration;
  }

  public static long runBashScript(@NotNull final Logger log, @NotNull final File workingDirectory, @NotNull final String... lines) throws IOException {
    final String script = StringUtil.join("\n", lines);
    final File file = FileUtil.createTempFile(workingDirectory, "script", ".sh", true);
    FileUtil.writeFile(file, script, "UTF-8");
    long time = runExecutable(log, "bash", workingDirectory, "-l", file.getAbsolutePath());
    FileUtil.delete(file);
    return time;
  }
}