

package jetbrains.buildServer.agent.ruby.rbenv;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class RbEnvShellRunner implements ShellScriptRunner {
  private static final Logger LOG = Logger.getInstance(RbEnvShellRunner.class.getName());
  private final InstalledRbEnv myRbEnv;

  protected RbEnvShellRunner(@NotNull final InstalledRbEnv rbEnv) {
    myRbEnv = rbEnv;
  }

  @NotNull
  public ExecResult run(@NotNull final String script,
                        @NotNull final String workingDirectory,
                        @Nullable final Map<String, String> environment) {
    final File directory = new File(workingDirectory);
    File scriptFile = null;
    try {
      try {
        scriptFile = FileUtil.createTempFile(directory, "rbenv", ".sh", true);
        FileUtil.writeFileAndReportErrors(scriptFile, script);
      } catch (IOException e) {
        LOG.error("Failed to create temp file, error: ", e);
        final ExecResult result = new ExecResult();
        result.setStderr("Failed to create temp file, error: " + e.getMessage());
        result.setException(e);
        return result;
      }

      // Patching environment
      final HashMap<String, String> environment1 = new HashMap<String, String>();
      if (environment != null) {
        environment1.putAll(environment);
      }
      environment1.put(Constants.RBENV_ROOT_ENV_VARIABLE, myRbEnv.getHome().getAbsolutePath());
      environment1.put(Constants.RBENV_VERSION_ENV_VARIABLE, getVersion());

      return RunnerUtil.run(workingDirectory, environment1, "/bin/sh", scriptFile.getAbsolutePath());
    } finally {
      try {
        if (scriptFile != null) {
          FileUtil.delete(scriptFile);
        }
      } catch (SecurityException ignored) {
      }
    }
  }

  public abstract String getVersion();
}