/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.feature;

import com.intellij.util.PathUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.SharedParams;
import jetbrains.buildServer.agent.rakerunner.SharedParamsType;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.RubySDKUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.SdkUtil;
import jetbrains.buildServer.agent.ruby.rbenv.RbEnvPathsSettings;
import jetbrains.buildServer.agent.ruby.rbenv.RbEnvRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRCBasedRubySdkImpl;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConfiguration;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConstants;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public class RubyEnvConfiguratorService implements BuildRunnerPrecondition {

  public static final String ENVS_TO_UNSET_PARAM = "teamcity.ruby.env.conf.feature.envs.to.unset";

  public RubyEnvConfiguratorService(@NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher) {
    dispatcher.addListener(new AgentLifeCycleAdapter() {
      @Override
      public void buildFinished(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
        RVMRCBasedRubySdkImpl.clearCache();
      }
    });
  }

  public void canStart(@NotNull final BuildRunnerContext context) throws RunBuildException {
    // check if feature is enabled
    final Collection<AgentBuildFeature> features =
      context.getBuild().getBuildFeaturesOfType(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE);
    if (features.isEmpty()) {
      return;
    }
    final Map<String, String> featureParameters = features.iterator().next().getParameters();

    final RubyEnvConfiguratorConfiguration configuration = new RubyEnvConfiguratorConfiguration(featureParameters);

    RVMPathsSettings.getInstanceEx().initialize(context.getBuildParameters().getEnvironmentVariables());
    RbEnvPathsSettings.getInstance().initialize(context.getBuildParameters().getEnvironmentVariables());

    final SharedParams sharedParams = new SharedParams();

    // Configure runner parameters
    configureSharedParameters(configuration, sharedParams);
    sharedParams.applyToContext(context);

    final RubySdk sdk;

    // validate params:
    try {
      validateConfiguratorParams(configuration);
    } catch (RakeTasksBuildService.InvalidConfigurationException e) {
      if (configuration.isShouldFailBuildIfNoSdkFound() || !e.isCanBeIgnored()) {
        // fail build
        throw new RunBuildException(e.getMessage());
      }
    }
    // try to create sdk, it will validate paths
    try {
      sdk = RubySDKUtil.createAndSetupSdk(context.getRunnerParameters(), context);
    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      if (configuration.isShouldFailBuildIfNoSdkFound() || !e.isCanBeIgnored()) {
        // fail build
        throw new RunBuildException(e.getMessage());
      }

      // else just show warning and quit:
      context.getBuild().getBuildLogger().warning(e.getMessage());
      return;
    }

    // validation has passed. let's path environment
    final EnvironmentPatchableMap newEnv = patchRunnerEnvironment(context, sdk, configuration, sharedParams);
    sharedParams.applyToContext(context);
    if (sdk instanceof RVMRCBasedRubySdkImpl) {
      RVMRCBasedRubySdkImpl.cache(sdk, newEnv, ((RVMRCBasedRubySdkImpl)sdk).getPathToRVMRCFolder());
    }
  }

  protected EnvironmentPatchableMap patchRunnerEnvironment(@NotNull final BuildRunnerContext context,
                                                           @NotNull final RubySdk sdk,
                                                           @NotNull final RubyEnvConfiguratorConfiguration configuration,
                                                           @NotNull final SharedParams sharedParams) throws RunBuildException {

    // editable env variables
    final Map<String, String> old = context.getBuildParameters().getEnvironmentVariables();
    final EnvironmentPatchableMap env = new EnvironmentPatchableMap(old);

    try {

      // Inspect env, warn about any problems
      RVMSupportUtil.inspectCurrentEnvironment(env, sdk, context.getBuild().getBuildLogger());

      // Save patched env variables to runnerEnvParams
      if (SdkUtil.isRvmSdk(sdk) && !sdk.isSystem()) {
        // true rvm sdk
        RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, env);
      } else {
        // fake or non-rvm sdk
        if (SdkUtil.isRvmSdk(sdk)) {
          RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, env);
        }

        if (SdkUtil.isRbEnvSdk(sdk)) {
          ((RbEnvRubySdk)sdk).patchEnvironment(env);
        }

        final Map<String, String> runParams = context.getRunnerParameters();
        final Map<String, String> buildParams = context.getBuildParameters().getAllParameters();

        // if checkout dir isn't ok for bundler path here, user may specify it using system property
        // see RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH.
        final String checkoutDirPath = context.getBuild().getCheckoutDirectory().getCanonicalPath();
        RubySDKUtil.patchPathEnvForNonRvmOrSystemRvmSdk(sdk, runParams, buildParams, env, checkoutDirPath);
      }

      Collection<String> envsToUnset = new ArrayList<String>();
      for (String key : old.keySet()) {
        if (!env.containsKey(key)) {
          envsToUnset.add(key);
        }
      }
      context.addRunnerParameter(ENVS_TO_UNSET_PARAM, StringUtil.join(envsToUnset, ","));

      // apply updated env variables to context:
      for (Map.Entry<String, String> keyAndValue : env.entrySet()) {
        context.addEnvironmentVariable(keyAndValue.getKey(), keyAndValue.getValue());
      }

      // success, mark that shared params were successfully applied
      sharedParams.setApplied(true);
      return env;
    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      // only show error msg, it is user-friendly
      throw new RunBuildException(e.getMessage());
    } catch (Exception e) {
      throw new RunBuildException(e);
    }
  }

  private void validateConfiguratorParams(@NotNull final RubyEnvConfiguratorConfiguration configuration)
    throws RakeTasksBuildService.InvalidConfigurationException {
    switch (configuration.getType()) {
      case RVM: {
        // sdk name
        if (StringUtil.isEmpty(configuration.getRVMSdkName())) {
          throw new RakeTasksBuildService.InvalidConfigurationException(
            "RVM interpreter name cannot be empty. If you want to use system ruby interpreter please enter 'system'.", true);
        }
        break;
      }
      case RVMRC: {
        String rvmrcFilePath = StringUtil.emptyIfNull(configuration.getRVMRCFilePath());
        if (!StringUtil.isEmptyOrSpaces(rvmrcFilePath) &&
            !PathUtil.getFileName(rvmrcFilePath).equals(".rvmrc")) {
          throw new RakeTasksBuildService.InvalidConfigurationException(
            "RVMRC file name must be '.rvmrc'. RVM does not support other names", false);
        }
        break;
      }
      case RBENV: {
        if (StringUtil.isEmpty(configuration.getRbEnvVersion())) {
          throw new RakeTasksBuildService.InvalidConfigurationException(
            "rbenv interpreter name cannot be empty. If you want to use system ruby interpreter please enter 'system'.", true);
        }
        break;
      }
      case RBENV_FILE: {
        String path = StringUtil.emptyIfNull(configuration.getRVMRCFilePath());
        if (!StringUtil.isEmptyOrSpaces(path) &&
            !PathUtil.getFileName(path).equals(".rbenv-version")) {
          throw new RakeTasksBuildService.InvalidConfigurationException(
            "local version file name must be '.rbenv-version'. rbenv does not support other names", false);
        }
        break;
      }
    }
  }

  private void configureSharedParameters(@NotNull final RubyEnvConfiguratorConfiguration configuration,
                                         @NotNull final SharedParams shared) {
    switch (configuration.getType()) {
      case INTERPRETER_PATH: {
        // ruby path
        shared.setInterpreterPath(configuration.getRubySdkPath());
        shared.setType(SharedParamsType.INTERPRETER_PATH);
        break;
      }
      case RVM: {
        // sdk name
        // gemset
        shared.setRVMSdkName(configuration.getRVMSdkName());
        shared.setRVMGemsetName(configuration.getRVMGemsetName());
        shared.setRVMGemsetCreate(configuration.isRVMGemsetCreate());
        shared.setType(SharedParamsType.RVM);
        break;
      }
      case RVMRC: {
        // .rvmrc path
        shared.setRVMRCPath(configuration.getRVMRCFilePath());
        shared.setType(SharedParamsType.RVMRC);
        break;
      }
      case RBENV: {
        shared.setRbEnvVersion(configuration.getRbEnvVersion());
        shared.setType(SharedParamsType.RBENV);
        break;
      }
      case RBENV_FILE: {
        shared.setRbEnvVersionFile(configuration.getRbEnvVersionFile());
        shared.setType(SharedParamsType.RBENV_FILE);
        break;
      }
    }
  }
}
