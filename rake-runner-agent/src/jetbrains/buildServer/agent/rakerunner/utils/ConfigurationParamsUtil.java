

package jetbrains.buildServer.agent.rakerunner.utils;

import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class ConfigurationParamsUtil implements RakeRunnerConstants {

  public static boolean isParameterEnabled(@NotNull final Map<String, String> params, @NotNull final String key) {
    return params.containsKey(key)
           && params.get(key).equals(Boolean.TRUE.toString());
  }

  public static boolean isTraceStagesOptionEnabled(@NotNull final Map<String, String> runParams) {
    return isParameterEnabled(runParams, SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED);
  }

  public static void setParameterEnabled(@NotNull final Map<String, String> runParams,
                                         @NotNull final String frameworkUIProperty,
                                         final boolean isEnabled) {
    runParams.put(frameworkUIProperty, Boolean.valueOf(isEnabled).toString());
  }
}