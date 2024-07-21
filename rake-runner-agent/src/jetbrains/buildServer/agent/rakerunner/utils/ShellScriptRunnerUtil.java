

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class ShellScriptRunnerUtil {
  private static final Logger LOG = Logger.getInstance(ShellScriptRunnerUtil.class.getName());

  public static void makeScriptFileExecutable(@NotNull final File scriptFile) throws IOException {
    setPermissions(scriptFile, "u+x");
  }

  private static void setPermissions(@NotNull final File script, @NotNull final String perms) throws IOException {
    Process process = Runtime.getRuntime().exec(new String[]{"chmod", perms, script.getAbsolutePath()});
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      LOG.error("Failed to execute chmod " + perms + " " + script.getAbsolutePath() + ", error: " + e);
    }
  }
}