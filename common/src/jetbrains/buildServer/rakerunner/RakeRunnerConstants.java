/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.Constants;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 03.06.2007
 */
public interface RakeRunnerConstants extends Constants {
    @NonNls public String RUNNER_TYPE = "rake-runner";
    @NonNls String DEBUG_PROPERTY = "rakeRunner.debug";

// Server properties
    /**
     * UI property: Rake task name
     */
    @NonNls String SERVER_UI_RAKE_TASKS_PROPERTY = "ui.rakeRunner.rake.tasks.names";

    @NonNls String SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED = "ui.rakeRunner.rake.trace.invoke.exec.stages.enabled";
    @NonNls String RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED_KEY = "teamcity.rake.runner.rake.trace.invoke.exec.stages.enabled";

    @NonNls String SERVER_UI_RAKE_TEST_UNIT_TESTOPTS_PROPERTY = "ui.rakeRunner.test.unit.options";
    @NonNls String RAKE_TEST_UNIT_TESTOPTS_PARAM_NAME = "TESTOPTS";

    @NonNls String SERVER_UI_RSPEC_SPEC_OPTS_PROPERTY = "ui.rakeRunner.rspec.specoptions";
    @NonNls String RAKE__RSPEC_SPEC_OPTS_PARAM_NAME = "SPEC_OPTS";

    @NonNls String SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY = "ui.rakeRunner.additional.rake.cmd.params";

    @NonNls String SERVER_UI_RUBY_INTERPRETER = "ui.rakeRunner.ruby.interpreter";

// Agent properties:
    @NonNls String ORIGINAL_SDK_AUTORUNNER_PATH_KEY = "rake.runner.original.sdk.test.unit.autorunner.path";
    @NonNls String RUBYLIB_ENVIRONMENT_VARIABLE = "RUBYLIB";

// Rake
    @NonNls String RAKE_CMDLINE_OPTIONS_RAKEFILE = "--rakefile";

// Teamcity Rake Runner Debug and logs
    @NonNls String RAKE_RUNNER_DEBUG_KEY = "teamcity.rake.runner.debug.mode";
}