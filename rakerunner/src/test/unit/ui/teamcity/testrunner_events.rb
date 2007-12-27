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

require 'test/unit/ui/teamcity/message_factory'
require 'test/unit/ui/teamcity/event_queue/event_queue'

module Test
  module Unit
    module UI
      module TeamCity
        module EventHandlers
          include Rake::TeamCity::Logger
          include Test::Unit::Util::BacktraceFilter

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
            debug_log("Test suit finished: #{@suite_name} in #{elapsed_time} seconds")
            log_one(Rake::TeamCity::MessageFactory.create_close_block(@suite_name, :test_suite))
          end

          # Reset all results from previous test suites. Occurs before each suite running.
          def reset_ui(count)
            # clear faults_list for test suite
            #fault_list.clear
          end

          ########################### Tests events #######################

          # Test case started
          def test_started(test_name)
            debug_log("Test started #{test_name}...")

            close_test_block unless @my_running_test_name

            @my_running_test_name = test_name
            log_one(Rake::TeamCity::MessageFactory.create_open_block(@my_running_test_name, :test))
          end

          # Test case finished
          def test_finished(test_name)
            #TODO - test output!!!!!!

            debug_log("Test finished #{test_name}...")
            close_test_block
          end

          # Test fault
          def add_fault(fault)
            case fault
            when Test::Unit::Failure
              backtrace = fault.location.to_s
              message = fault.message.to_s
              test_name = fault.test_name
              debug_log("Add failure for #{test_name}")
            when Test::Unit::Error
              backtrace = filter_backtrace(fault.exception.backtrace).join("\n    ")
              message = "#{fault.exception.class.name}: #{fault.exception.message.to_s}" 
              test_name = fault.test_name
              debug_log("Add error for #{test_name}")
            else
              test_name =
                ((defined? fault.test_name) || @my_running_test_name || "<unknown>").to_s
              message = ((defined? fault.message) || fault).to_s
              backtrace = fault.to_s
              debug_log("Add unknown fault #{test_name}")
            end
            log_one(Rake::TeamCity::MessageFactory.
                    create_test_problem_message(test_name, message,
                                                message + "\n\n    " + backtrace ))
          end

          # Test result changed - update statistics
          def result_changed(result)
            # TODO - add special message type and show statistics in teamcity task
#            debug_log("result_changed: all#{result.run_count.to_s}, " +
#                                      "asserts#{result.assertion_count.to_s}, " +
#                                      "failure#{result.failure_count.to_s}, " +
#                                      "error count#{result.error_count.to_s}")
          end
          ###########################################################################

          def close_test_block
            if @my_running_test_name
              log_one(Rake::TeamCity::MessageFactory.create_close_block(@my_running_test_name, :test))
              @my_running_test_name = nil
            end
          end

          def debug_log(string)
            # Does nothing
            # Override method to see log
            #
            puts("{TC_TR_DEBUG_LOG} #{string}")
          end
        end
      end
    end
  end
end