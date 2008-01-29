# Copyright 2000-2008 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @date: 02.06.2007
if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/rakerunner_consts'
else
  require 'test/unit/ui/teamcity/rakerunner_consts'
end

if ENV[TEAMCITY_RAKERUNNER_LOG_PATH_KEY]
  RUNNER_LOG = File.new(ENV[TEAMCITY_RAKERUNNER_LOG_PATH_KEY] + "/rakeRunner_testrunner.log", "a+");
  RUNNER_LOG << "\n[#{Time.now}] : Started\n";
end

require 'test/unit/ui/testrunnermediator'
require 'test/unit/ui/testrunnerutilities'

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/testrunner_events'
else
  require 'test/unit/ui/teamcity/testrunner_events'
end

# Runs a Test::Unit::TestSuite on teamcity server.
class Test::Unit::UI::TeamCity::TestRunner
  extend Test::Unit::UI::TestRunnerUtilities

  # Includes module with event handlers
  include Test::Unit::UI::TeamCity::EventHandlers

  # Creates a new TestRunner for running the passed
  # suite.
  def initialize(suite, output_level=NORMAL)

    if (suite.respond_to?(:suite))
      @suite = suite.suite
    else
      @suite = suite
    end
    @result = nil
  end


  # Starts testing
  def start
    setup_mediator
    attach_to_mediator

    msg_dispatcher =  Rake::TeamCity.msg_dispatcher
    manual_start = !msg_dispatcher.started?

    # open xml-rpc connection
    msg_dispatcher.start_dispatcher if manual_start

    start_mediator

    # close xml-rpc connection
    msg_dispatcher.stop_dispatcher if manual_start

    @result
  end

  def start_mediator
    return @mediator.run_suite
  end

  private

  def setup_mediator
    @mediator = Test::Unit::UI::TestRunnerMediator.new(@suite)
    @suite_name = (@suite.kind_of?(Module) ? @suit.name : @suite.to_s)
  end

  def attach_to_mediator
    @mediator.add_listener(Test::Unit::TestResult::FAULT, &method(:add_fault))
    @mediator.add_listener(Test::Unit::TestResult::CHANGED, &method(:result_changed))

    @mediator.add_listener(Test::Unit::TestCase::STARTED, &method(:test_started))
    @mediator.add_listener(Test::Unit::TestCase::FINISHED, &method(:test_finished))

    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::STARTED, &method(:started))
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::FINISHED, &method(:finished))
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::RESET, &method(:reset_ui))
  end
end

if __FILE__ == $0
  Test::Unit::UI::TeamCity::TestRunner.start_command_line_test
end

at_exit do
  if ENV[TEAMCITY_RAKERUNNER_LOG_PATH_KEY]
    RUNNER_LOG << "[#{Time.now}] : Finished\n\n";
    RUNNER_LOG.close
  end
end