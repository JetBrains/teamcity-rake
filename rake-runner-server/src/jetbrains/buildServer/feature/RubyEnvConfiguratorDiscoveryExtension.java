/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
