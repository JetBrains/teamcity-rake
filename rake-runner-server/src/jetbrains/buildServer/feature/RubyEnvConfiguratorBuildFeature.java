/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman.Chernyatchik
 */
public class RubyEnvConfiguratorBuildFeature extends BuildFeature implements BuildStartContextProcessor {
  private final String myEditUrl;

  public RubyEnvConfiguratorBuildFeature(@NotNull final PluginDescriptor descriptor) {
    myEditUrl = descriptor.getPluginResourcesPath("rubyEnvConfiguratorParams.jsp");
  }


  @NotNull
  @Override
  public String getType() {
    return "ruby.env.configurator";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Ruby environment configurator";
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

    if ("true".equals(context.getSharedParameters().get(RubyEnvConfiguratorUtil.UI_USE_RVM_KEY))) {
      context.addSharedParameter(RubyEnvConfiguratorUtil.UI_USE_RVM_KEY, "manual");
    }

    final Collection<SBuildFeatureDescriptor> buildFeatures = buildType.getBuildFeatures();
    for (SBuildFeatureDescriptor bf : buildFeatures) {
      if (!buildType.isEnabled(bf.getId())) continue;
      // if our type
      if (getType().equals(bf.getType())) {
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
    final RubyEnvConfiguratorConfiguration configuration = new RubyEnvConfiguratorConfiguration(params);
    switch (configuration.getType()) {
      case INTERPRETER_PATH: {
        final String sdkPath = configuration.getRubySdkPath();

        result.append("Interpreter path: ").append(sdkPath != null ? sdkPath : "default");
        break;
      }
      case RVM: {
        final String rvmSdkName = configuration.getRVMSdkName();
        final String gemset = configuration.getRVMGemsetName();

        result.append("RVM interpreter: ").append(rvmSdkName != null ? rvmSdkName : RakeRunnerBundle.DEFAULT_RVM_SDK);
        if (gemset != null) {
          result.append('@').append(gemset);
        }
        break;
      }
      case RVMRC: {
        final String rvmrcPath = configuration.getRVMRCFilePath();

        result.append("Path to a '.rvmrc' file:").append(rvmrcPath != null ? rvmrcPath : "default");
        break;
      }
    }

    if (configuration.isShouldFailBuildIfNoSdkFound()) {
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
