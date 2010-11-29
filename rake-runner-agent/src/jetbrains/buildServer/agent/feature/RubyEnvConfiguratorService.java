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

package jetbrains.buildServer.agent.feature;

import com.intellij.util.containers.HashMap;
import java.io.File;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.BuildRunnerPrecondition;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.RubyLightweightSdk;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import jetbrains.buildServer.agent.rakerunner.SharedRubyEnvSettings;
import jetbrains.buildServer.agent.rakerunner.utils.RubySDKUtil;
import jetbrains.buildServer.feature.RubyEnvConfiguratorUtil;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Roman.Chernyatchik
 */
public class RubyEnvConfiguratorService implements BuildRunnerPrecondition {
  private static final String RUBY_CONFIGURATOR_ERROR_TYPE = "RUBY_CONFIGURATOR_ERROR";

  public void canStart(@NotNull final BuildRunnerContext context) throws RunBuildException {
    final Map<String, String> configParameters = context.getBuild().getSharedConfigParameters();

    // check if is enabled
    if (!RubyEnvConfiguratorUtil.isRubyEnvConfiguratorEnabled(configParameters)) {
      return;
    }

    if (!RubyEnvConfiguratorUtil.shouldFailBuildIfNoSdkFound(configParameters)) {
      return;
    }

    try {
      // validate params:
      validateConfiguratorParams(configParameters);

      // try to create sdk, it will validate paths
      createSdk(context);

    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      throw new RunBuildException(e.getMessage());
    }
  }

  private void validateConfiguratorParams(final Map<String, String> configParameters) throws RakeTasksBuildService.MyBuildFailureException {
    if (!RubyEnvConfiguratorUtil.isRVMEnabled(configParameters)) {
      // sdk name
      final String sdkName = RubyEnvConfiguratorUtil.getRVMSdkName(configParameters);

      if (StringUtil.isEmpty(sdkName)) {
        throw new RakeTasksBuildService.MyBuildFailureException("RVM interpreter name cannot be empty. If you want to use system ruby interpreter please enter 'system'.");
      }
    }
  }

  public RubyEnvConfiguratorService(final EventDispatcher<AgentLifeCycleListener> dispatcher) {
    dispatcher.addListener(new AgentLifeCycleAdapter() {
      @Override
      public void beforeRunnerStart(@NotNull final BuildRunnerContext context) {
        super.beforeRunnerStart(context);

        final Map<String, String> configParameters = context.getBuild().getSharedConfigParameters();

        // check if is enabled
        if (!RubyEnvConfiguratorUtil.isRubyEnvConfiguratorEnabled(configParameters)) {
          return;
        }

        // editable env variables
        final Map<String, String> runnerEnvParams = new HashMap<String, String>(context.getBuildParameters().getEnvironmentVariables());

        try {
          // Configure runner parameters
          passRubyParamsToRunner(context, configParameters);

          // validate params:
          validateConfiguratorParams(configParameters);

          // Create sdk & save patched env variables to runnerEnvParams
          final RubyLightweightSdk sdk = createSdk(context);
          if (sdk.isRVMSdk()) {
            // rvm sdk
            RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, runnerEnvParams);
          } else {
            // not rvm sdk
            final Map<String, String> runParams = context.getRunnerParameters();
            final Map<String, String> buildParams = context.getBuildParameters().getAllParameters();
            final Map<String, String> buildEnvVars = context.getBuildParameters().getEnvironmentVariables();

            final RubySdk heavySdk = RubySDKUtil.createAndSetupSdk(runParams, buildParams, buildEnvVars);

            // TODO[romeo]: better to take it from some other place. Fortunatelly it's a quite rare usecase.
            final File checkoutDirectoryOfSomeStep = context.getBuild().getCheckoutDirectory();
            RubySDKUtil.patchEnvForNonRVMSdk(heavySdk, runParams, buildParams, runnerEnvParams,
                                             checkoutDirectoryOfSomeStep != null ? checkoutDirectoryOfSomeStep.getCanonicalPath() : null);
          }

          // apply updated env variables to context:
          for (Map.Entry<String, String> keyAndValue : runnerEnvParams.entrySet()) {
            context.addEnvironmentVariable(keyAndValue.getKey(), keyAndValue.getValue());
          }

          // succes!!!
          context.addRunnerParameter(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_APPLIED, Boolean.TRUE.toString());

        } catch (RakeTasksBuildService.MyBuildFailureException e) {
          context.getBuild().getBuildLogger().internalError(RUBY_CONFIGURATOR_ERROR_TYPE, e.getMessage(), null);
        } catch (Exception e) {
          context.getBuild().getBuildLogger().internalError(RUBY_CONFIGURATOR_ERROR_TYPE, e.getMessage(), e);
        }
      }
    });
  }

  @NotNull
  private RubyLightweightSdk createSdk(final BuildRunnerContext context)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    return RubySDKUtil.createAndSetupLightweightSdk(context.getRunnerParameters(),
                                                    context.getBuildParameters().getAllParameters());
  }

  private void passRubyParamsToRunner(final BuildRunnerContext context, final Map<String, String> configParameters) {
    context.addRunnerParameter(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_SET, Boolean.TRUE.toString());
    if (RubyEnvConfiguratorUtil.isRVMEnabled(configParameters)) {
      // ruby path
      final String rubySdkPath = RubyEnvConfiguratorUtil.getRubySdkPath(configParameters);
      context.addRunnerParameter(SharedRubyEnvSettings.SHARED_RUBY_INTERPRETER_PATH,
                                 rubySdkPath != null ? rubySdkPath : "");
    } else {
      // sdk name
      final String sdkName = RubyEnvConfiguratorUtil.getRVMSdkName(configParameters);
      context.addRunnerParameter(SharedRubyEnvSettings.SHARED_RUBY_RVM_SDK_NAME,
                                 sdkName != null ? sdkName : "");
      // gemset
      final String gemsetName = RubyEnvConfiguratorUtil.getRVMGemsetName(configParameters);
      context.addRunnerParameter(SharedRubyEnvSettings.SHARED_RUBY_RVM_GEMSET_NAME,
                                 gemsetName != null ? gemsetName : "");
    }
  }
}
