

package jetbrains.buildServer.feature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 8.1
 */
public class RubyEnvConfiguratorDiscoveryExtension /*implements BuildFeatureDiscoveryExtension*/ {
  @Nullable
  public List<DiscoveredObject> discover(@NotNull final BuildTypeSettings settings) {
    if (!settings.getBuildFeaturesOfType(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE).isEmpty()) return null;

    if (settings.findBuildRunnerByType(RakeRunnerConstants.RUNNER_TYPE) != null) {
      // TODO: It's better to obtain file browser and check for '.rvmrc', etc.
      final Map<String, String> rvm = new HashMap<String, String>(4);
      rvm.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rvmrc");
      rvm.put(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY, ".rvmrc");
      rvm.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS, Boolean.TRUE.toString());
      rvm.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());

      final Map<String, String> rbenv = new HashMap<String, String>(3);
      rbenv.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rbenv_file");
      rbenv.put(RubyEnvConfiguratorConstants.UI_RBENV_FILE_PATH_KEY, ".rbenv-version");
      rbenv.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());

      return Arrays.asList(new DiscoveredObject(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE, rvm),
                           new DiscoveredObject(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE, rbenv));
    }
    return null;
  }
}