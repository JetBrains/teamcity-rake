/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class RubyEnvConfiguratorBuildFeature extends BuildFeature implements BuildStartContextProcessor {
  private final String myEditUrl;

  public RubyEnvConfiguratorBuildFeature(@NotNull final PluginDescriptor descriptor) {
    myEditUrl = descriptor.getPluginResourcesPath("rubyRuntimeContextParams.jsp");
  }


  @NotNull
  @Override
  public String getType() {
    return "ruby.env.configurator";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Ruby Environment Configurator";
  }

  @Override
  public String getEditParametersUrl() {
    return myEditUrl;
  }

  public void updateParameters(@NotNull final BuildStartContext context) {
    final SBuildType buildType = context.getBuild().getBuildType();
    if (buildType == null) {
      return;
    }

    final String rubyEnvConfiguratorFeatureType = getType();

    final Collection<SBuildFeatureDescriptor> buildFeatures = buildType.getBuildFeatures();
    for (SBuildFeatureDescriptor bf : buildFeatures) {
      // if our type
      if (rubyEnvConfiguratorFeatureType.equals(bf.getType())) {
        // mark that feature is enabled
        context.addSharedParameter(RubyEnvConfiguratorUtil.RUBY_ENV_CONFIGURATOR_KEY, Boolean.TRUE.toString());

        // copy feature settings to context
        for (final Map.Entry<String, String> param : bf.getParameters().entrySet()) {
          if (param.getValue() != null) {
            context.addSharedParameter(param.getKey(), param.getValue());
          }
        }
        // multiple such features for build are not allowed
        break;
      }
    }
  }

  @Override
  public boolean isMultipleFeaturesPerBuildTypeAllowed() {
    return false;
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull final Map<String, String> params) {
    StringBuilder result = new StringBuilder();
    if (RubyEnvConfiguratorUtil.isRVMEnabled(params)) {
      final String sdkPath = RubyEnvConfiguratorUtil.getRubySdkPath(params);
      result.append("Interpreter path: ").append(sdkPath != null ? sdkPath : "default");
    } else {
      final String rvmSdkName = RubyEnvConfiguratorUtil.getRVMSdkName(params);

      result.append("RVM interpreter: ").append(rvmSdkName != null ? rvmSdkName
                                                                   : RakeRunnerBundle.DEFAULT_RVM_SDK);
      final String gemset = RubyEnvConfiguratorUtil.getRVMGemsetName(params);
      if (gemset != null) {
        result.append('@').append(gemset);
      }
    }
    if (RubyEnvConfiguratorUtil.shouldFailBuildIfNoSdkFound(params)) {
      result.append("\n").append("Fail build if Ruby interpreter wasn't found.");
    }
    return result.toString();
  }

  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> defaults = new HashMap<String, String>(1);

    defaults.put(RubyEnvConfiguratorUtil.UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());

    return defaults;
  }
}
