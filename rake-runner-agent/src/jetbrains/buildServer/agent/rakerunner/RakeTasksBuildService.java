/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner;

import com.intellij.util.containers.HashMap;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.utils.*;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.runner.BuildFileRunnerUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;
import jetbrains.buildServer.runner.CommandLineArgumentsUtil;

import java.io.File;
import java.util.*;

import static jetbrains.buildServer.agent.rakerunner.utils.FileUtil.getCanonicalPath;
import static jetbrains.buildServer.runner.BuildFileRunnerConstants.BUILD_FILE_PATH_KEY;

/**
 * @author Roman.Chernyatchik
 */
public class RakeTasksBuildService extends BuildServiceAdapter implements RakeRunnerConstants {
  private final Set<File> myFilesToDelete = new HashSet<File>();
  private final String RSPEC_RUNNER_OPTIONS_REQUIRE_KEY = "--require";
  private final String RSPEC_RUNNER_OPTIONS_FORMATTER_PATH = "teamcity/spec/runner/formatter/teamcity/formatter";
  private final String RSPEC_RUNNERR_OPTIONS_FORMATTER_KEY = "--format";
  private final String RSPEC_RUNNERR_OPTIONS_FORMATTER_CLASS = "Spec::Runner::Formatter::TeamcityFormatter";

  private final String CUCUMBER_RUNNER_OPTIONS_EXPAND_KEY = "--expand";
  private final String CUCUMBER_RUNNER_OPTIONS_FORMAT_KEY = "--format";
  private final String CUCUMBER_RUNNER_OPTIONS_FORMAT_CLASS = "Teamcity::Cucumber::Formatter";


  @NotNull
  @Override
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    List<String> arguments = new ArrayList<String>();
    final Map<String, String> runParams = new HashMap<String, String>(getRunnerParameters());
    final Map<String, String> buildParams = new HashMap<String, String>(getBuildParameters().getAllParameters());

    // apply options converter
    SupportedTestFramework.convertOptionsIfNecessary(runParams);

    // runParams - all server-ui options
    // buildParams - system properties (system.*), environment vars (env.*)

    final Map<String, String> buildEnvVars = getBuildParameters().getEnvironmentVariables();
    final Map<String, String> runnerEnvParams = new HashMap<String, String>(buildEnvVars);

    final File buildFile = getBuildFile(runParams);

    final RakeRunnerUtils.RubyConfigMode interpreterConfigMode = RakeRunnerUtils.getRubyInterpreterConfigMode(runParams);
    final boolean rubyEnvAlreadyConfigured = Boolean.valueOf(runParams.get(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_SET));


    try {
      // configure params in terms of "shared params"
      configureRunnerParams(interpreterConfigMode, rubyEnvAlreadyConfigured, runParams);

      final String checkoutDirPath = getCanonicalPath(getCheckoutDirectory());

      // Sdk
      final RubySdk sdk = RubySDKUtil.createAndSetupSdk(runParams, buildParams, buildEnvVars);

      if (!(interpreterConfigMode == RakeRunnerUtils.RubyConfigMode.DEFAULT && rubyEnvAlreadyConfigured)) {
        // 1. default, but build feature wasn't configured
        // 2. rvm or interpreter path

        // Inspect env, warn about any problems
        // (if defaults were set by smb else we cannot check them)
        RVMSupportUtil.inspectCurrentEnvironment(runnerEnvParams, sdk, getBuild().getBuildLogger());

        if (sdk.isRvmSdk()) {
          // Patch env for RVM
          RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, runnerEnvParams);

          if (sdk.isSystemRvm()) {
            // Also Patch path for fake RVM
            RubySDKUtil.patchPathEnvForNonRvmOrSystemRvmSdk(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath);
          }
        } else {
          if (interpreterConfigMode == RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH) {
            // non-rvm sdk
            RubySDKUtil.patchPathEnvForNonRvmOrSystemRvmSdk(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath);
          }
        }
      }

      // loadpath patch for test runners
      addTestRunnerPatchFiles(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath);

      // attached frameworks info
      if (SupportedTestFramework.isAnyFrameworkActivated(runParams)) {
        runnerEnvParams.put(RAKERUNNER_USED_FRAMEWORKS_KEY,
                            SupportedTestFramework.getActivatedFrameworksConfig(runParams));

      }

      // track invoke/execute stages
      if (ConfigurationParamsUtil.isTraceStagesOptionEnabled(runParams)) {
        runnerEnvParams.put(RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY, Boolean.TRUE.toString());
      }

      // Rake runner script
      final String rakeRunnerPath;
      final String customRakeRunnerScript = buildParams.get(CUSTOM_RAKERUNNER_SCRIPT);
      if (!StringUtil.isEmpty(customRakeRunnerScript)) {
        // use custom runner
        rakeRunnerPath = customRakeRunnerScript;
      } else {
        // default one
        rakeRunnerPath = RubyProjectSourcesUtil.getRakeRunnerPath();
      }
      arguments.add(rakeRunnerPath);

      // Rake gem version
      addGemVersionAttribute(arguments, RAKE_GEM_VERSION_PROPERTY, buildParams);

      // Rake options
      // Custom Rakefile if specified
      if (buildFile != null) {
        arguments.add(RAKE_CMDLINE_OPTIONS_RAKEFILE);
        arguments.add(buildFile.getAbsolutePath());
      }

      // Other arguments
      final String userDefinedArgs = runParams.get(SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY);
      if (!TextUtil.isEmptyOrWhitespaced(userDefinedArgs)) {
        addCmdlineArguments(arguments, userDefinedArgs);
      }

      // Tasks names
      final String tasks_names = runParams.get(SERVER_UI_RAKE_TASKS_PROPERTY);
      if (!PropertiesUtil.isEmptyOrNull(tasks_names)) {
        addCmdlineArguments(arguments, tasks_names);
      }

      // rspec
      attachRSpecFormatterIfNeeded(runParams, runnerEnvParams);

      // cucumber
      attachCucumberFormatterIfNeeded(runParams, runnerEnvParams);

      // Bunlde exec emulation:
      // (do not do it befor RVM Env patch!!!!!!)
      BundlerUtil.enableBundleExecEmulationIfNeeded(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath);

      // Result:
      return new SimpleProgramCommandLine(runnerEnvParams,
                                          getWorkingDirectory().getAbsolutePath(),
                                          sdk.getInterpreterPath(),
                                          arguments);
    } catch (MyBuildFailureException e) {
      throw new RunBuildException(e.getMessage(), e);
    }
  }

  private void configureRunnerParams(@NotNull final RakeRunnerUtils.RubyConfigMode interpreterConfigMode,
                                     final boolean rubyEnvAlreadyConfigured,
                                     final Map<String, String> runParams) throws MyBuildFailureException {
    switch (interpreterConfigMode) {
      case DEFAULT:
        if (rubyEnvAlreadyConfigured) {
          // check that params were applied
          if (!Boolean.valueOf(runParams.get(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_APPLIED))) {
            throw new MyBuildFailureException(
              "Ruby interpeter is configured outside Rake build runner but configuration settings weren't applied. No sense to launch rake.");
          }
        } else {
          // if shared params are not defined - use default system interpreter
          runParams.put(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_SET, Boolean.TRUE.toString());
          runParams.put(SharedRubyEnvSettings.SHARED_RUBY_INTERPRETER_PATH, "");
        }
        break;
      case INTERPRETER_PATH:
        final String rubySdkPath = RakeRunnerUtils.getRubySdkPath(runParams);

        if (StringUtil.isEmpty(rubySdkPath)) {
          throw new MyBuildFailureException("Ruby interpeter path isn't specified.");
        }
        runParams.put(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_SET, Boolean.TRUE.toString());
        runParams.put(SharedRubyEnvSettings.SHARED_RUBY_INTERPRETER_PATH,
                      rubySdkPath);
        break;
      case RVM:
        final String rvmSdkName = RakeRunnerUtils.getRVMSdkName(runParams);

        if (StringUtil.isEmpty(rvmSdkName)) {
          throw new MyBuildFailureException("RVM Ruby interpeter name isn't specified.");
        }

        runParams.put(SharedRubyEnvSettings.SHARED_RUBY_PARAMS_ARE_SET, Boolean.TRUE.toString());
        runParams.put(SharedRubyEnvSettings.SHARED_RUBY_RVM_SDK_NAME, rvmSdkName);
        runParams.put(SharedRubyEnvSettings.SHARED_RUBY_RVM_GEMSET_NAME, RakeRunnerUtils.getRVMGemsetName(runParams));
        break;
    }
  }

  /**
   * Specify gem version attribute if property is set
   * @param arguments Cmdline arguments
   * @param gemVersionProperty Property name
   * @param buildParams  Build params
   */
  private void addGemVersionAttribute(final List<String> arguments,
                                      final String gemVersionProperty,
                                      final Map<String, String> buildParams) {
    final String rakeGemVersion = buildParams.get(gemVersionProperty);
    if (!StringUtil.isEmpty(rakeGemVersion)) {
      arguments.add("_" + rakeGemVersion + "_");
    }
  }

  @Override
  public void afterProcessFinished() {
    // Remove tmp files
    for (File file : myFilesToDelete) {
      jetbrains.buildServer.util.FileUtil.delete(file);
    }
    myFilesToDelete.clear();
  }

  private void attachRSpecFormatterIfNeeded(final Map<String, String> runParams,
                                            final Map<String, String> env) {

    //attach RSpec formatter only if spec reporter enabled
    if (SupportedTestFramework.RSPEC.isActivated(runParams)) {
      final StringBuilder buff = new StringBuilder();

      final String userSpecOpts = runParams.get(SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY);
      if (!StringUtil.isEmpty(userSpecOpts)) {
        buff.append(userSpecOpts.trim()).append(' ');
      }
      buff.append(RSPEC_RUNNER_OPTIONS_REQUIRE_KEY).append(' ');
      buff.append(RSPEC_RUNNER_OPTIONS_FORMATTER_PATH).append(' ');
      buff.append(RSPEC_RUNNERR_OPTIONS_FORMATTER_KEY).append(' ');
      buff.append(RSPEC_RUNNERR_OPTIONS_FORMATTER_CLASS);

      final String specOpts = buff.toString();

      // Log for user
      getLogger().message("RSpec Options: " + specOpts);

      // Set env variable
      env.put(RAKE_RSPEC_OPTS_PARAM_NAME, specOpts);
    }
  }

  private void attachCucumberFormatterIfNeeded(
    final Map<String, String> runParams,
    final Map<String, String> env) {

    //attach Cucumber formatter only if cucumber reporter enabled
    if (SupportedTestFramework.CUCUMBER.isActivated(runParams)) {
      final StringBuilder buff = new StringBuilder();

      //TODO use additional options when cucumber will support it!
      //cmd.addParameter(RAKE_CUCUMBER_OPTS_PARAM_NAME + "=" + CUCUMBER_RUNNER_INIT_OPTIONS);

      final String userCucumberOpts = runParams.get(SERVER_UI_RAKE_CUCUMBER_OPTS_PROPERTY);
      if (!StringUtil.isEmpty(userCucumberOpts)) {
        buff.append(userCucumberOpts.trim()).append(' ');
      }
      buff.append(CUCUMBER_RUNNER_OPTIONS_EXPAND_KEY).append(' ');
      buff.append(CUCUMBER_RUNNER_OPTIONS_FORMAT_KEY).append(' ');
      buff.append(CUCUMBER_RUNNER_OPTIONS_FORMAT_CLASS);

      final String cucumberOpts = buff.toString();

      // Log for user
      getLogger().message("Cucumber Options: " + cucumberOpts);

      // Set env variable
      env.put(RAKE_CUCUMBER_OPTS_PARAM_NAME, cucumberOpts);
    }
  }

  private void addTestRunnerPatchFiles(@NotNull final RubySdk sdk,
                                       @NotNull final Map<String, String> runParams,
                                       @NotNull final Map<String, String> buildParams,
                                       @NotNull final Map<String, String> runnerEnvParams,
                                       @NotNull final String checkoutDirPath)
      throws MyBuildFailureException, RunBuildException {


    final StringBuilder buff = new StringBuilder();

    // common part - for rake taks and tests
    buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_Common());

    // Enable Test::Unit patch for : test::unit, test::spec and shoulda frameworks
    if (SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams)) {
      buff.append(File.pathSeparatorChar);

      final String testUnitPatchRoot = RubyProjectSourcesUtil.getLoadPath_PatchRoot_TestUnit();
      buff.append(testUnitPatchRoot);

      // for bundler support
      if (BundlerUtil.isBundleExecEmulationEnabled(runParams)) {
        runnerEnvParams.put(TEAMCITY_TESTUNIT_REPORTER_PATCH_LOCATION, testUnitPatchRoot);
      }

      // due to patching loadpath we replace original autorunner but it is used buy our tests runner
      runnerEnvParams.put(ORIGINAL_SDK_AUTORUNNER_PATH_KEY,
                          TestUnitUtil.getSDKTestUnitAutoRunnerScriptPath(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath));
      runnerEnvParams.put(ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY,
                          TestUnitUtil.getSDKTestUnitTestRunnerMediatorScriptPath(sdk, runParams, buildParams, runnerEnvParams, checkoutDirPath));
    }

    // for bdd frameworks
    if (SupportedTestFramework.CUCUMBER.isActivated(runParams)
        || SupportedTestFramework.RSPEC.isActivated(runParams)) {
      buff.append(File.pathSeparatorChar);
      buff.append(RubyProjectSourcesUtil.getLoadPath_PatchRoot_Bdd());
    }

    // patch loadpath
    OSUtil.appendToRUBYLIBEnvVariable(buff.toString(), runnerEnvParams);
  }

  private void addCmdlineArguments(@NotNull final List<String> argsList, @NotNull final String argsString) {
    argsList.addAll(CommandLineArgumentsUtil.extractArguments(argsString));
  }

  @Nullable
  private File getBuildFile(Map<String, String> runParameters) throws RunBuildException {
    final File buildFile;
    if (BuildFileRunnerUtil.isCustomBuildFileUsed(runParameters)) {
      buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
      myFilesToDelete.add(buildFile);
    } else {
      final String buildFilePath = runParameters.get(BUILD_FILE_PATH_KEY);
      if (PropertiesUtil.isEmptyOrNull(buildFilePath)) {
        //use rake defaults
        buildFile = null;
      } else {
        buildFile = BuildFileRunnerUtil.getBuildFile(runParameters);
      }
    }
    return buildFile;
  }

  public static class MyBuildFailureException extends Exception {
    public MyBuildFailureException(@NotNull final String msg) {
      super(msg);
    }
  }

}
