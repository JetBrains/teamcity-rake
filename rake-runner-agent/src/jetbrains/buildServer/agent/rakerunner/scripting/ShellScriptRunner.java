

package jetbrains.buildServer.agent.rakerunner.scripting;

import java.util.Map;
import jetbrains.buildServer.ExecResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public interface ShellScriptRunner extends ScriptRunner {

  @NotNull
  ExecResult run(@NotNull final String script,
                 @NotNull final String workingDirectory,
                 @Nullable final Map<String, String> environment);
}