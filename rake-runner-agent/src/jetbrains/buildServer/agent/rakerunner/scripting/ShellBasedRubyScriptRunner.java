

package jetbrains.buildServer.agent.rakerunner.scripting;

import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class ShellBasedRubyScriptRunner implements jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner {
  private final ShellScriptRunner myShellScriptRunner;

  public ShellBasedRubyScriptRunner(@NotNull final ShellScriptRunner shellScriptRunner) {
    myShellScriptRunner = shellScriptRunner;
  }

  @NotNull
  public ExecResult run(@NotNull final String script,
                        @NotNull final String workingDirectory,
                        @Nullable final Map<String, String> environment,
                        @NotNull final String... rubyArgs) {
    return myShellScriptRunner.run(rubyScriptToShellScript(script, rubyArgs), workingDirectory, environment);
  }

  public static String rubyScriptToShellScript(@NotNull final String rubyScript, @NotNull final String... rubyArgs) {
    final StringBuilder sb = new StringBuilder();
    sb.append("ruby");
    for (String rubyArg : rubyArgs) {
      sb.append(' ').append(rubyArg);
    }
    sb.append(" -e ");
    sb.append('"').append(StringUtil.quoteReplacement(rubyScript)).append('"');
    return sb.toString();
  }

}