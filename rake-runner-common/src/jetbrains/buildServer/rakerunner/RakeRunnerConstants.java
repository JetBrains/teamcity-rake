/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.rakerunner;

import org.jetbrains.annotations.NonNls;

/**
 * @author Roman Chernyatchik
 */
public interface RakeRunnerConstants {
  // After updating config verson please also update settings converter (in RakeRunnerRunType)
  // and update config verson in settings edit ui (taskRunnerRunParams.jsp). Last one is needed for correct conversion
  // if user copied old-style build configuration and then updated some settings in it.
  @NonNls String CURRENT_CONFIG_VERSION = "2";

  @NonNls String RUNNER_TYPE = "rake-runner";
  @NonNls String AGENT_BUNDLE_JAR = "rake-runner.jar";

// Server properties

  // task name
  @NonNls String SERVER_UI_RAKE_TASKS_PROPERTY = "ui.rakeRunner.rake.tasks.names";

  // trace/invoke
  @NonNls String SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED = "ui.rakeRunner.rake.trace.invoke.exec.stages.enabled";
  @NonNls String RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY = "TEAMCITY_RAKE_TRACE";

  // Additional CMD params
  @NonNls String SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY = "ui.rakeRunner.additional.rake.cmd.params";
  @NonNls String SERVER_UI_RUBY_INTERPRETER_ADDITIONAL_PARAMS = "ui.rakeRunner.ruby.interpreter.additional.params";

  // Explicit Ruby interpreter lpath
  @NonNls String SERVER_UI_RUBY_INTERPRETER_PATH = "ui.rakeRunner.ruby.interpreter.path";
  @NonNls String SERVER_UI_RUBY_RVM_GEMSET_NAME = "ui.rakeRunner.ruby.rvm.gemset.name";
  @NonNls String SERVER_UI_RUBY_RVM_SDK_NAME = "ui.rakeRunner.ruby.rvm.sdk.name";
  @NonNls String SERVER_UI_RUBY_USAGE_MODE = "ui.rakeRunner.ruby.use.mode";

  // Bundler
  @NonNls String SERVER_UI_BUNDLE_EXEC_PROPERTY = "ui.rakeRunner.bunlder.exec.enabled";


  // Test Frameworks

  // Test::Unit
  @NonNls String SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY = "ui.rakeRunner.frameworks.testunit.enabled";

  // RSpec
  @NonNls String SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY = "ui.rakeRunner.frameworks.rspec.enabled";
  @NonNls String SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY = "ui.rakeRunner.rspec.specoptions";
  @NonNls String RAKE_RSPEC_OPTS_PARAM_NAME = "SPEC_OPTS";

  // Test-Spec
  @NonNls String SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY = "ui.rakeRunner.frameworks.testspec.enabled";

  // Shoulda
  @NonNls String SERVER_UI_RAKE_SHOULDA_ENABLED_PROPERTY = "ui.rakeRunner.frameworks.shoulda.enabled";

  // Cucumber
  @NonNls String SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY = "ui.rakeRunner.frameworks.cucumber.enabled";
  @NonNls String SERVER_UI_RAKE_CUCUMBER_OPTS_PROPERTY = "ui.rakeRunner.cucumber.options";
  @NonNls String RAKE_CUCUMBER_OPTS_PARAM_NAME = "CUCUMBER_OPTS";

  @NonNls String SERVER_CONFIGURATION_VERSION_PROPERTY = "ui.rakeRunner.config.version";

// Agent properties:
  // Custom rake tasks runner script
  @NonNls String CUSTOM_RAKERUNNER_SCRIPT = "system.teamcity.rake.runner.custom.runner";

  // Teamcity Rake Runner Debug and logs
  @NonNls String DEBUG_PROPERTY = "system.teamcity.rake.runner.debug.mode";

  // Forced gem version:
  @NonNls String RAKE_GEM_VERSION_PROPERTY = "system.teamcity.rake.runner.gem.rake.version";
  @NonNls String TEST_UNIT_GEM_VERSION_PROPERTY = "system.teamcity.rake.runner.gem.testunit.version";
  @NonNls String BUNDLER_GEM_VERSION_PROPERTY = "system.teamcity.rake.runner.gem.bundler.version";
  @NonNls String TEST_UNIT_USE_BUILTIN_VERSION_PARAM = "built-in";

  // SDK hack
  @NonNls String RUBYLIB_ENVIRONMENT_VARIABLE = "RUBYLIB";
  @NonNls String RUBYOPT_ENVIRONMENT_VARIABLE = "RUBYOPT";
  @NonNls String ORIGINAL_SDK_AUTORUNNER_PATH_KEY = "TEAMCIY_RAKE_TU_AUTORUNNER_PATH";
  @NonNls String ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY = "TEAMCITY_RAKE_TU_TESTRUNNERMADIATOR_PATH";
  @NonNls String ORIGINAL_SDK_19_MINITEST_UNIT_SCRIPT_PATH_KEY = "TC_RUBY19_SDK_MINITEST_RUNNER_PATH_KEY";
  @NonNls String TEAMCITY_TESTUNIT_REPORTER_PATCH_LOCATION = "RUBYMINE_TESTUNIT_REPORTER";

  // Bundler
  @NonNls String CUSTOM_GEMFILE_RELATIVE_PATH = "system.teamcity.rake.runner.custom.gemfile";
  @NonNls String CUSTOM_BUNDLE_FOLDER_PATH = "system.teamcity.rake.runner.custom.bundle.path";
  @NonNls String GEMFILE_RESOLVE_IN_CHECKOUT_DIRECTORY = "system.teamcity.rake.runner.bundle.resolve.in.checkout.dir";

  // Rake
  @NonNls String RAKE_CMDLINE_OPTIONS_RAKEFILE = "--rakefile";

  // Attached frameworks
  @NonNls String RAKERUNNER_USED_FRAMEWORKS_KEY = "TEAMCITY_RAKE_RUNNER_USED_FRAMEWORKS";
}
