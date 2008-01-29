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
    @NonNls String SERVER_UI_RAKE_TASK_PROPERTY = "rakeRunner.rake.task.name";
    @NonNls String SERVER_UI_RAKE_OPTION_TRACE_PROPERTY = "rakeRunner.rake.options.trace";
    @NonNls String AGENT_CMD_LINE_RAKE_OPTION_TRACE_FLAG = "--trace";
    @NonNls String SERVER_UI_RAKE_OPTION_QUITE_PROPERTY = "rakeRunner.rake.options.quite";
    @NonNls String AGENT_CMD_LINE_RAKE_OPTION_QUITE_FLAG = "--quite";
    //TODO verbose, silent

// Agent properties:
    
    // Ruby interpreter
    @NonNls String SYSTEM_PROPERTY_RUBY_INTERPRETER = "system.ruby.interpreter";
    @NonNls String TARGET_RUBY_INTERPRETER = "target.ruby.interpreter";
    @NonNls String ENV_VARIABLE_RUBY_INTERPRETER = "RUBY_INTERPRETER";

    @NonNls String ORIGINAL_SDK_AUTORUNNER_PATH_KEY = "rake.runner.original.sdk.test.unit.autorunner.path";
    @NonNls String RUBYLIB_ENVIRONMENT_VARIABLE = "RUBYLIB";
}
