/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
  @NonNls public String RUNNER_TYPE = "rake-runner";
  @NonNls String RAKE_RUNNER_SERVER_PLUGIN_FILE_NAME = "rake-runner-server.jar";
  @NonNls String AGENT_BUNDLE_JAR = "rake-runner-agent.jar";

// Server properties

  // task name
  @NonNls String SERVER_UI_RAKE_TASKS_PROPERTY = "ui.rakeRunner.rake.tasks.names";

  @NonNls String RAKE_MODE_KEY = "teamcity.rake.runner.mode";
  @NonNls String RAKE_MODE_BUILDSERVER = "buildserver";

  // trace/invoke
  @NonNls String SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED = "ui.rakeRunner.rake.trace.invoke.exec.stages.enabled";
  @NonNls String RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY = "teamcity.rake.runner.rake.trace.invoke.exec.stages.enabled";

  // TEST_OPTS
  @NonNls String SERVER_UI_RAKE_TEST_UNIT_TESTOPTS_PROPERTY = "ui.rakeRunner.test.unit.options";
  @NonNls String RAKE_TEST_UNIT_TESTOPTS_PARAM_NAME = "TESTOPTS";

  // SPEC_OPTS
  @NonNls String SERVER_UI_RSPEC_SPEC_OPTS_PROPERTY = "ui.rakeRunner.rspec.specoptions";
  @NonNls String RAKE__RSPEC_SPEC_OPTS_PARAM_NAME = "SPEC_OPTS";

  // Additional CMD params
  @NonNls String SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY = "ui.rakeRunner.additional.rake.cmd.params";

  // Explicit Ruby interpreter lpath
  @NonNls String SERVER_UI_RUBY_INTERPRETER = "ui.rakeRunner.ruby.interpreter";

  // Enable rake output capturer
  @NonNls String SERVER_UI_RAKE_OUTPUT_CAPTURER_ENABLED = "ui.rakeRunner.rake.output.capturer.enabled";

// Agent properties:

  // Teamcity Rake Runner Debug and logs
  @NonNls String DEBUG_PROPERTY = "system.teamcity.rake.runner.debug.mode";

  // SDK hack
  @NonNls String RUBYLIB_ENVIRONMENT_VARIABLE = "RUBYLIB";
  @NonNls String ORIGINAL_SDK_AUTORUNNER_PATH_KEY = "rake.runner.original.sdk.test.unit.autorunner.path";
  @NonNls String ORIGINAL_SDK_TESTRUNNERMEDIATOR_PATH_KEY = "rake.runner.original.sdk.test.unit.testrunnermadiator.path";

  @NonNls String LOG_OUTPUT_CAPTURER_DISABLED_KEY = "teamcity.rake.runner.debug.output.capturer.disabled";

  // Rake
  @NonNls String RAKE_CMDLINE_OPTIONS_RAKEFILE = "--rakefile";
}