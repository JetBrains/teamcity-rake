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
  require 'src/test/unit/ui/teamcity/rakerunner_utils'
  require 'src/test/unit/ui/teamcity/rakerunner_consts'

  require 'src/test/unit/ui/teamcity/message_factory'
  require 'src/test/unit/ui/teamcity/event_queue/messages_dispatcher'
  require 'src/test/unit/ui/teamcity/std_capture_helper'
  require 'src/test/unit/ui/teamcity/runner_utils'
else
  require 'test/unit/ui/teamcity/rakerunner_utils'
  require 'test/unit/ui/teamcity/rakerunner_consts'

  require 'test/unit/ui/teamcity/message_factory'
  require 'test/unit/ui/teamcity/event_queue/messages_dispatcher'
  require 'test/unit/ui/teamcity/std_capture_helper'
  require 'test/unit/ui/teamcity/runner_utils'
end

module Test
  module Unit
    module UI
      module TeamCity        
        module EventHandlers
          include Rake::TeamCity::Logger
          include Rake::TeamCity::StdCaptureHelper
          include Test::Unit::Util::BacktraceFilter
          include Rake::TeamCity::RunnerUtils

          ###########################################3
          def log_one(msg)
            Rake::TeamCity.msg_dispatcher.log_one(msg)
          end

          #################### TestSuite events ####################
          # Test suit started
          def started(result)
            @result = result

            debug_log("Test suit started: #{@suite_name}...")
            log_one(Rake::TeamCity::MessageFactory.create_open_block(@suite_name, :test_suite))
          end

          # Test suit stopped
          def finished(elapsed_time)
            # Total statistic
            statistics = @result.to_s
            log_one(Rake::TeamCity::MessageFactory.create_message(statistics))
            debug_log(@result.to_s)

            # Time statistic from Spec Runner
            status_message = "Test suit finished: #{elapsed_time} seconds"
            log_one(Rake::TeamCity::MessageFactory.create_progress_message(status_message))
            debug_log(status_message)

            log_one(Rake::TeamCity::MessageFactory.create_close_block(@suite_name, :test_suite))
          end

          # Reset all results from previous test suites. Occurs before each suite running.
          def reset_ui(count)
            # Do nothing
          end

          ########################### Tests events #######################

          # Test case started
          def test_started(test_name)
            teamcity_test_name = convert_ruby_test_name(test_name)
            debug_log("Test started #{test_name}...[#{teamcity_test_name}]")

            capture_output_start

            @my_running_test_name = teamcity_test_name
            @my_running_test_name_ruby = test_name
            log_one(Rake::TeamCity::MessageFactory.create_open_block(@my_running_test_name, :test))
          end

          # Test case finished
          def test_finished(test_name)
            assert_test_valid(test_name)
            
            stdout_string, stderr_string = capture_output_end
            if (!stdout_string.empty?)
              log_one(Rake::TeamCity::MessageFactory.create_test_output_message(@my_running_test_name, true, stdout_string))
            end
            debug_log("My stdOut: [#{stdout_string}]")
            if (!stderr_string.empty?)
              log_one(Rake::TeamCity::MessageFactory.create_test_output_message(@my_running_test_name, false, stderr_string))
            end
            debug_log("My stdErr: [#{stderr_string}]")

            debug_log("Test finished #{@my_running_test_name_ruby}...[#{@my_running_test_name}]")
            close_test_block
          end

          # Test fault
          def add_fault(fault)
            case fault
            when Test::Unit::Failure
              if fault.location.kind_of?(Array)
                backtrace = fault.location.join("\n    ")
              else
                backtrace = fault.location.to_s
              end
              message = fault.message.to_s
              test_name = fault.test_name
              debug_log("Add failure for #{test_name}, \n    Backtrace:    \n#{backtrace}")
            when Test::Unit::Error
              backtrace = filter_backtrace(fault.exception.backtrace).join("\n    ")
              message = "#{fault.exception.class.name}: #{fault.exception.message.to_s}" 
              test_name = fault.test_name
              debug_log("Add error for #{test_name}, \n    Backtrace:    \n#{backtrace}")
            else
              test_name =
                ((defined? fault.test_name) || @my_running_test_name || "<unknown>").to_s
              message = ((defined? fault.message) || fault).to_s
              backtrace = fault.to_s
              debug_log("Add unknown fault #{test_name}, \n    Backtrace:    \n#{backtrace}")
            end

            assert_test_valid(test_name)
            log_one(Rake::TeamCity::MessageFactory.
                    create_test_problem_message(@my_running_test_name, message,
                                                message + "\n\n    " + backtrace))
          end

          # Test result changed - update statistics
          def result_changed(result)
            debug_log("result_changed: all=#{result.run_count.to_s}, " +
                                      "asserts=#{result.assertion_count.to_s}, " +
                                      "failure=#{result.failure_count.to_s}, " +
                                      "error count=#{result.error_count.to_s}")
          end
          ###########################################################################

          def close_test_block
            if @my_running_test_name
              log_one(Rake::TeamCity::MessageFactory.create_close_block(@my_running_test_name, :test))
              @my_running_test_name = nil
            end
          end

          private

          def assert_test_valid(test_name)
            if (test_name != @my_running_test_name_ruby)
              teamcity_test_name = convert_ruby_test_name(test_name)
              msg = "Finished test '#{test_name}'[#{teamcity_test_name}] doesn't correspond to current running test '#{@my_running_test_name_ruby}'[#{@my_running_test_name}]!"
              debug_log(msg)
              raise Rake::TeamCity::InnerException, msg, caller 
            end
          end

          def debug_log(string)
            # Logs output.
            if ENV[TEAMCITY_RAKERUNNER_LOG_PATH_KEY]
              UNIT_TESTS_RUNNER_LOG.log_msg(string);
            end
            # Uncomment to see output in Teamcity build log.
            # puts "{TC_TR_DEBUG_LOG} #{string}\n";
          end
        end
      end
    end
  end
end