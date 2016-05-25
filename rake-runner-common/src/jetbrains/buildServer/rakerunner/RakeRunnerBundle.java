/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

/**
 * @author Roman.Chernyatchik
 */

public interface RakeRunnerBundle {
  String DEFAULT_RVM_SDK = "system";

  String RUNNER_DESCRIPTION = "Runner for executing Rake tasks, Test::Unit and RSpec tests";
  String RUNNER_DISPLAY_NAME = "Rake";

  String RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT = "Failed to run Rake..";
  String RUNNER_ERROR_TITLE_JRUBY_PROBLEMS_IN_CONF_ON_AGENT = "Failed to run JRuby..";
  String MSG_OS_NOT_SUPPORTED = "OS isn't supported!";
}
