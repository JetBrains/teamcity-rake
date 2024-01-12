

package jetbrains.buildServer.agent.ruby.rvm.detector;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.ConfigurationApplier;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for "RVM Detector" - an utility designed for
 * detecting RVM installation on agent.
 *
 * @author Vladislav.Rassokhin
 */
public abstract class RVMDetector {

  public static final String CONF_PARAMETER_PREFIX = "rvm.";
  public static final String CONF_RVM_RUBIES_LIST = CONF_PARAMETER_PREFIX + "rubies.list";
  public static final String RVM_PATH_ENV_VARIABLE = "rvm_path";

  /**
   * That function detects installed RVM.
   *
   * @param environmentParams environment variables map
   * @return founded RVM installation or null if RVM does not found
   */
  @Nullable
  public abstract InstalledRVM detect(@NotNull final Map<String, String> environmentParams);

  public void patchBuildAgentConfiguration(@NotNull final ConfigurationApplier configuration, @Nullable final InstalledRVM rvm) {
    if (rvm == null) {
      return;
    }

    configuration.addEnvironmentVariable(RVM_PATH_ENV_VARIABLE, rvm.getPath());

    // TODO: do not provide this parameter, install ruby if necessary
    String allVersions = StringUtil.join(",", rvm.getInstalledRubies());
    configuration.addConfigurationParameter(CONF_RVM_RUBIES_LIST, allVersions);
  }
}