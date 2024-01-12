

package jetbrains.buildServer.agent.ruby;

import java.io.File;
import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public interface RubySdk {
  @Nullable
  File getHome();

  @NotNull
  File getRubyExecutable();

  @Nullable
  RubyVersionManager getVersionManager();

  @Nullable
  String getGemset();

  @NotNull
  String getName();

  @Nullable
  String getVersion();

  boolean isSystem();

  boolean isRuby19();

  boolean isJRuby();

  @NotNull
  String[] getGemPaths();

  @NotNull
  String[] getLoadPath();

  void setup(@NotNull final Map<String, String> env);

  @NotNull
  ExecResult getGemPathsFetchLog();

  @NotNull
  ExecResult getLoadPathsFetchLog();

  @NotNull
  RubyScriptRunner getScriptRunner();
}