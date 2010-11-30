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
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.BuildRunnerPrecondition;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.RubyLightweightSdk;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import jetbrains.buildServer.agent.rakerunner.SharedRubyEnvSettings;
import jetbrains.buildServer.agent.rakerunner.utils.RubySDKUtil;
import jetbrains.buildServer.feature.RubyEnvConfiguratorUtil;
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

    final boolean shouldFailBuildIfNoSdkFound = RubyEnvConfiguratorUtil.shouldFailBuildIfNoSdkFound(configParameters);

    // Configure runner parameters
    passRubyParamsToRunner(context, configParameters);

    final RubyLightweightSdk sdk;

    // validate params:
    try {
      validateConfiguratorParams(configParameters);

      // try to create sdk, it will validate paths
      sdk = createSdk(context);

    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      if (shouldFailBuildIfNoSdkFound) {
        // fail build
        throw new RunBuildException(e.getMessage());
      }

      // else just show warning and quit:
      logInternalError(e.getMessage(), null, context);
      return;
    }

    // validation has passed. let's path environment
    patchRunnerEnvironment(context, sdk);
  }

  private void patchRunnerEnvironment(@NotNull final BuildRunnerContext context,
                                      @NotNull final RubyLightweightSdk sdk) {

    // editable env variables
    final Map<String, String> runnerEnvParams = new HashMap<String, String>(context.getBuildParameters().getEnvironmentVariables());

    try {

      // Inspect env, warn about any problems
      RVMSupportUtil.inspectCurrentEnvironment(runnerEnvParams, sdk, context.getBuild().getBuildLogger());

      // Save patched env variables to runnerEnvParams
      if (sdk.isRVMSdk()) {
        // rvm sdk
        RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, runnerEnvParams);
      } else {
        // not rvm sdk
        final Map<String, String> runParams = context.getRunnerParameters();
        final Map<String, String> buildParams = context.getBuildParameters().getAllParameters();
        final Map<String, String> buildEnvVars = context.getBuildParameters().getEnvironmentVariables();

        final RubySdk heavySdk = RubySDKUtil.createAndSetupSdk(runParams, buildParams, buildEnvVars);

        // if checkout dir isn't ok for bundler path here, user may specify it using system property
        // see RakeRunnerConstants.CUSTOM_BUNDLE_FOLDER_PATH.
        final File checkoutDirectory = context.getBuild().getCheckoutDirectory();
        final String checkoutDirPath = checkoutDirectory != null ? checkoutDirectory.getCanonicalPath()
                                                                 : null;
        RubySDKUtil.patchEnvForNonRVMSdk(heavySdk, runParams, buildParams, runnerEnvParams, checkoutDirPath);
      }

      // apply updated env variables to context:
      for (Map.Entry<String, String> keyAndValue : runnerEnvParams.entrySet()) {
        context.addEnvironmentVariable(keyAndValue.getKey(), keyAndValue.getValue());
      }

      // succes, mark that shared params were succesfully applied
      context.addRunnerParameter(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_APPLIED, Boolean.TRUE.toString());

    } catch (RakeTasksBuildService.MyBuildFailureException e) {
      // only show error msg, it is user-friendly
      logInternalError(e.getMessage(), null, context);
    } catch (Exception e) {
      logInternalError(e.getMessage(), e, context);
    }
  }

  private void logInternalError(final String message, final Throwable throwable, final BuildRunnerContext context) {
    context.getBuild().getBuildLogger().internalError(RUBY_CONFIGURATOR_ERROR_TYPE, message, throwable);
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
