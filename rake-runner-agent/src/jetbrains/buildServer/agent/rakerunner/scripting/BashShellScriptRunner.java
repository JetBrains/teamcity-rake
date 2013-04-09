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
