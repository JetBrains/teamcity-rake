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

package jetbrains.buildServer.agent.feature;

import com.intellij.util.containers.HashMap;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.BuildRunnerPrecondition;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.SharedParams;
import jetbrains.buildServer.agent.rakerunner.SharedParamsType;
import jetbrains.buildServer.agent.rakerunner.utils.RubySDKUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.agent.ruby.rvm.detector.RVMDetector;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConfiguration;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public class RubyEnvConfiguratorService implements BuildRunnerPrecondition {

  @NotNull
  private final RVMDetector myRVMDetector;

  public RubyEnvConfiguratorService(@NotNull final RVMDetector myRVMDetector) {
    this.myRVMDetector = myRVMDetector;
  }

  public void canStart(@NotNull final BuildRunnerContext context) throws RunBuildException {
    final Map<String, String> configParameters = context.getBuild().getSharedConfigParameters();

    final RubyEnvConfiguratorConfiguration configuration = new RubyEnvConfiguratorConfiguration(configParameters);

    // check if is enabled
    if (configuration.getType() == RubyEnvConfiguratorConfiguration.Type.OFF) {
      return;
    }

    @Nullable final InstalledRVM rvm = myRVMDetector.detect(context.getBuildParameters().getEnvironmentVariables());
    RVMPathsSettings.getInstanceEx().initialize(rvm);

    final SharedParams sharedParams = new SharedParams();

    // Configure runner parameters
    configureSharedParameters(configuration, sharedParams);
    sharedParams.applyToContext(context);

    final RubySdk sdk;

    // validate params:
    try {
      validateConfiguratorParams(configuration);

      // try to create sdk, it will validate paths
      sdk = RubySDKUtil.createAndSetupSdk(context.getRunnerParameters(), context.getBuildParameters());

    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      if (configuration.isShouldFailBuildIfNoSdkFound()) {
        // fail build
        throw new RunBuildException(e.getMessage());
      }

      // else just show warning and quit:
      context.getBuild().getBuildLogger().warning(e.getMessage());
      return;
    }

    // validation has passed. let's path environment
    patchRunnerEnvironment(context, sdk, configuration, sharedParams);
    sharedParams.applyToContext(context);
  }

  protected void patchRunnerEnvironment(@NotNull final BuildRunnerContext context,
                                        @NotNull final RubySdk sdk,
                                        @NotNull final RubyEnvConfiguratorConfiguration configuration,
                                        @NotNull final SharedParams sharedParams) throws RunBuildException {

    // editable env variables
    final Map<String, String> runnerEnvParams = new HashMap<String, String>(context.getBuildParameters().getEnvironmentVariables());

    try {

      // Inspect env, warn about any problems
      RVMSupportUtil.inspectCurrentEnvironment(runnerEnvParams, sdk, context.getBuild().getBuildLogger());

      // Save patched env variables to runnerEnvParams
      if (sdk.isRvmSdk() && !sdk.isSystem()) {
        // true rvm sdk
        RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, runnerEnvParams);
      } else {
        // fake or non-rvm sdk
        if (sdk.isRvmSdk()) {
          RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, runnerEnvParams);
        }

        final Map<String, String> runParams = context.getRunnerParameters();
        final Map<String, String> buildParams = context.getBuildParameters().getAllParameters();

        // if checkout dir isn't ok for bundler path here, user may specify it using system property
        // see RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH.
        final String checkoutDirPath = context.getBuild().getCheckoutDirectory().getCanonicalPath();
        RubySDKUtil.patchPathEnvForNonRvmOrSystemRvmSdk(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath);
      }

      // apply updated env variables to context:
      for (Map.Entry<String, String> keyAndValue : runnerEnvParams.entrySet()) {
        context.addEnvironmentVariable(keyAndValue.getKey(), keyAndValue.getValue());
      }

      // succes, mark that shared params were succesfully applied
      sharedParams.setApplied(true);

    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      // only show error msg, it is user-friendly
      throw new RunBuildException(e.getMessage());
    } catch (Exception e) {
      throw new RunBuildException(e);
    }
  }

  private void validateConfiguratorParams(@NotNull final RubyEnvConfiguratorConfiguration configuration)
    throws RakeTasksBuildService.MyBuildFailureException {
    if (configuration.getType() == RubyEnvConfiguratorConfiguration.Type.RVM) {
      // sdk name
      if (StringUtil.isEmpty(configuration.getRVMSdkName())) {
        throw new RakeTasksBuildService.MyBuildFailureException(
          "RVM interpreter name cannot be empty. If you want to use system ruby interpreter please enter 'system'.");
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
        shared.setType(SharedParamsType.RVM);
        break;
      }
      case RVMRC: {
        // .rvmrc path
        shared.setRVMRCPath(configuration.getRVMRCFilePath());
        shared.setType(SharedParamsType.RVMRC);
        break;
      }
    }
  }
}
