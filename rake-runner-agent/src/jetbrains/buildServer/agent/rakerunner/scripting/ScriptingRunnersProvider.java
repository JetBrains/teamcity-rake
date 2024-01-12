

package jetbrains.buildServer.agent.rakerunner.scripting;

import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class ScriptingRunnersProvider {
  private static ScriptingRunnersProvider ourRVMDefaultRunnersProvider;

  @NotNull
  public abstract RubyScriptRunner getRubyScriptRunner();

  @NotNull
  public abstract ShellScriptRunner getShellScriptRunner();

  public static final ScriptingRunnersProvider RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER;

  static {
    RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER = new ScriptingRunnersProvider() {
      @NotNull
      @Override
      public RubyScriptRunner getRubyScriptRunner() {
        return new ShellBasedRubyScriptRunner(getShellScriptRunner());
      }

      @NotNull
      @Override
      public ShellScriptRunner getShellScriptRunner() {
        return RvmShellRunner.getRvmShellRunner();
      }
    };
    ourRVMDefaultRunnersProvider = RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER;
  }

  @NotNull
  public static ScriptingRunnersProvider getRVMDefault() {
    return ourRVMDefaultRunnersProvider;
  }

  public static void setRVMDefault(@NotNull final ScriptingRunnersProvider runnersProvider) {
    ourRVMDefaultRunnersProvider = runnersProvider;
  }
}