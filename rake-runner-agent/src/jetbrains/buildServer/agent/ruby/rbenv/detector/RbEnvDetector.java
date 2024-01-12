

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.ConfigurationApplier;
import jetbrains.buildServer.agent.ruby.rbenv.Constants;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for "rbenv Detector" - an utility designed for
 * detecting rbenv installation on agent.
 *
 * @author Vladislav.Rassokhin
 */
public abstract class RbEnvDetector {

  /**
   * That function detects installed rbenv.
   *
   * @param environmentParams environment variables map
   * @return founded rbenv installation or null if rbenv does not found
   */
  @Nullable
  public abstract InstalledRbEnv detect(@NotNull final Map<String, String> environmentParams);

  public void patchBuildAgentConfiguration(@NotNull final ConfigurationApplier configuration, @Nullable final InstalledRbEnv rbenv) {
    if (rbenv == null) {
      return;
    }

    configuration.addEnvironmentVariable(Constants.RBENV_ROOT_ENV_VARIABLE, getRootEnvVariable(rbenv));

    // TODO: do not provide this parameter, install ruby using (ruby-build) if necessary
    configuration.addConfigurationParameter(Constants.CONF_RBENV_RUBIES_LIST, getRbenvRubiesList(rbenv));
  }

  private static String getRbenvRubiesList(InstalledRbEnv rbenv) {
    return StringUtil.join(",", rbenv.getInstalledVersions());
  }

  @NotNull
  private static String getRootEnvVariable(@NotNull InstalledRbEnv rbenv) {
    return rbenv.getHome().getAbsolutePath();
  }
}