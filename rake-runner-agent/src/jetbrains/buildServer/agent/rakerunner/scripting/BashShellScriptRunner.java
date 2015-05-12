/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner.scripting;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class BashShellScriptRunner implements ShellScriptRunner {
  private static final Logger LOG = Logger.getInstance(BashShellScriptRunner.class.getName());

  @NotNull
  public ExecResult run(@NotNull final String script,
                        @NotNull final String workingDirectory,
                        @Nullable final Map<String, String> environment) {
    final File directory = new File(workingDirectory);
    File scriptFile = null;
    try {
      try {
        scriptFile = FileUtil.createTempFile(directory, "script", ".sh", true);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Created file " + scriptFile.getAbsolutePath());
        }
        FileUtil.writeFileAndReportErrors(scriptFile, script);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Script is:" + script);
        }
      } catch (IOException e) {
        LOG.error("Failed to create temp file, error: ", e);
        final ExecResult result = new ExecResult();
        result.setStderr("Failed to create temp file, error: " + e.getMessage());
        result.setException(e);
        return result;
      }
      final ExecResult run = RunnerUtil.run(workingDirectory, environment, "/bin/bash", scriptFile.getAbsolutePath());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Script ExecResult:" + run);
      }
      return run;
    } finally {
      try {
        if (scriptFile != null) {
          FileUtil.delete(scriptFile);
        }
      } catch (SecurityException ignored) {
      }
    }
  }
}
