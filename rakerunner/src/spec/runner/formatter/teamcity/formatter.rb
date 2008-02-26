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
# @date: 18:02:54

require 'spec/runner/formatter/base_formatter'

if ENV["idea.rake.debug.sources"]
  require 'src/utils/logger_util'
  require 'src/test/unit/ui/teamcity/rakerunner_utils'
  require 'src/test/unit/ui/teamcity/rakerunner_consts'
else
  require 'utils/logger_util'
  require 'test/unit/ui/teamcity/rakerunner_utils'
  require 'test/unit/ui/teamcity/rakerunner_consts'
end

SPEC_FORMATTER_LOG = Rake::TeamCity::Utils::RSpecFileLogger.new
SPEC_FORMATTER_LOG.log_msg("spec formatter.rb loaded.")

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/rakerunner_consts'

  require 'src/test/unit/ui/teamcity/message_factory'
  require 'src/test/unit/ui/teamcity/event_queue/messages_dispatcher'
  require 'src/test/unit/ui/teamcity/std_capture_helper'
  require 'src/test/unit/ui/teamcity/runner_utils'
else
  require 'test/unit/ui/teamcity/rakerunner_consts'

  require 'test/unit/ui/teamcity/message_factory'
  require 'test/unit/ui/teamcity/event_queue/messages_dispatcher'
  require 'test/unit/ui/teamcity/std_capture_helper'
  require 'test/unit/ui/teamcity/runner_utils'
end
module Spec
  module Runner
    module Formatter
      class TeamcityFormatter <  Spec::Runner::Formatter::BaseFormatter
        include Rake::TeamCity::Logger
        include Rake::TeamCity::StdCaptureHelper
        include Rake::TeamCity::RunnerUtils

        ########## Teamcity #############################
        def log_one(msg)
          SPEC_FORMATTER_LOG.log_block("msg.hash", "msg") do
            Rake::TeamCity.msg_dispatcher.log_one(msg)
          end
        end

        ######## Spec formatter ########################
        def initialize(arg1, arg2=nil)
          # Rspec 1.0.8 gem and higher rspec plugin support
          # 1. initialize(where)
          # 2. initialize(options, where)
          if (arg2)
            # initialize(options, where)
            super(arg1, nil)
          else
            # initialize(where)
            super(nil)
            @options = nil
          end

          @current_behaviour_number = 0
        end

        # The number of the currently running behaviour
        def current_behaviour_number
          @current_behaviour_number
        end

        def start(example_count)
          @example_count = example_count

          @msg_dispatcher =  Rake::TeamCity.msg_dispatcher
          @manual_start = !@msg_dispatcher.started?

          # open xml-rpc connection
          @msg_dispatcher.start_dispatcher if @manual_start
        end


        # For RSpec < 1.1
        def add_behaviour(name)
          super
          my_add_example_group(name)
        end

        #For RSpec >= 1.1.
        def add_example_group(example_group)
          super
          my_add_example_group(example_group.description)
        end

        def start_dump
          # Do nothing
        end

        def example_started(example)
          @my_running_example_name_real = example.description
          @my_running_example_name = "#{@example_group_desc} #{@my_running_example_name_real}"

          log_one(Rake::TeamCity::MessageFactory.create_open_block(@my_running_example_name, :test))
          debug_log("Example started -#{@my_running_example_name_real}... [#{@my_running_example_name}]")

          capture_output_start
          debug_log("Output caputre started.")
        end

        def example_passed(example)
          assert_example_valid(example.description)

          stop_capture_output_and_log_it

          close_test_block
        end

        def example_failed(example, counter, failure)
          assert_example_valid(example.description)

          stop_capture_output_and_log_it

          message =  failure.exception.nil? ? "[Without Exception]" : "#{failure.exception.class.name}: #{failure.exception.message}"
          backtrace = failure.exception.nil? ? "" : format_backtrace(failure.exception.backtrace)

          # failure description
          full_failure_description = message
          (full_failure_description += "\n\n    " + backtrace) if backtrace

          log_one(Rake::TeamCity::MessageFactory.create_test_problem_message(@my_running_example_name, message, full_failure_description))

          debug_log("Example failed #{@my_running_example_name}, Message:\n#{message} \n\nBackrace:]n#{backtrace}\n\nFull failure desc:\n#{full_failure_description}")

          close_test_block
        end

        def example_pending(example_group_desc, example, message)
          assert_example_valid(example.description)
          assert_example_group_valid(example_group_desc)

          stop_capture_output_and_log_it
          
          debug_log("Example pending #{@example_group_desc}.#{@my_running_example_name} - #{message}")

          log_one(Rake::TeamCity::MessageFactory.
                  create_test_ignored_message(message, @my_running_example_name))

          close_test_block
        end

# TODO see snippet_extractor.rb
# Here we can add file link or show code lined
#        def extra_failure_content(failure)
#          # @snippet_extractor.snippet(failure.exception)
#        end

        def dump_failure(counter, failure)
          # Do nothing
        end

        def dump_summary(duration, example_count, failure_count, pending_count)
          if dry_run?
            totals = "This was a dry-run"
          else
            totals = "#{example_count} example#{'s' unless example_count == 1}, #{failure_count} failure#{'s' unless failure_count == 1}, #{example_count - failure_count - pending_count} passed"
            totals << ", #{pending_count} pending" if pending_count > 0
          end

          close_example_group

          # Total statistic
          log_one(Rake::TeamCity::MessageFactory.create_message(totals))
          debug_log(totals)

          # Time statistic from Spec Runner
          status_message = "Finished in #{duration} seconds"
          log_one(Rake::TeamCity::MessageFactory.create_progress_message(status_message))
          debug_log(status_message)

          # close xml-rpc connection
          debug_log("Closing dispatcher..")
          @msg_dispatcher.stop_dispatcher if @manual_start
        end

        ###########################################################################
        private

        def dry_run?
          (@options && (@options.dry_run)) ? true : false
        end

        def backtrace_line(line)
          line.sub(/\A([^:]+:\d+)$/, '\\1:')
        end

        def format_backtrace(backtrace)
          return "" if backtrace.nil?
          backtrace.map { |line| backtrace_line(line) }.join("\n")
        end

        # Refactored initialize method. Is used for support rspec API < 1.1 and >= 1.1.
        def my_add_example_group(group_desc)
          @current_behaviour_number += 1
          # Let's close the previous block
          unless current_behaviour_number == 1
              close_example_group
          end

          # New block starts.
          @example_group_desc = "#{group_desc}"
          log_one(Rake::TeamCity::MessageFactory.create_open_block(@example_group_desc, :test_suite))
          debug_log("Add example group(behaviour): #{@example_group_desc}...")
        end


        def close_test_block
          if @my_running_example_name
            log_one(Rake::TeamCity::MessageFactory.create_close_block(@my_running_example_name, :test))
            debug_log("Example finished #{@my_running_example_name_real}...[#{@my_running_example_name}]")
            @my_running_example_name = nil
          end
        end

        def close_example_group
          log_one(Rake::TeamCity::MessageFactory.create_close_block(@example_group_desc, :test_suite))
          debug_log("Close example group(behaviour): #{@example_group_desc}.")
        end

        def debug_log(string)
          # Logs output.
          SPEC_FORMATTER_LOG.log_msg(string)

          # Uncomment to see output in Teamcity build log.
          # puts "{TC_TR_DEBUG_LOG} #{string}\n";
        end

        def assert_example_valid(example_desc)
           if (example_desc != @my_running_example_name_real)
              msg = "Example '#{example_desc}' doesn't correspond to current running example '#{@my_running_example_name}'!"
              debug_log(msg)
              raise Rake::TeamCity::InnerException, msg, caller
            end
        end

        def assert_example_group_valid(example_group_desc)
           if (example_group_desc !=  @example_group_desc)
              msg = "Example group(behaviour) '#{example_group_desc}' doesn't correspond to current running example group'#{ @example_group_desc}'!"
              debug_log(msg)
              raise Rake::TeamCity::InnerException, msg, caller
            end
        end

        def stop_capture_output_and_log_it
          stdout_string, stderr_string = capture_output_end
          debug_log("Example capturing was stopped.")
          if (!stdout_string.empty?)
            log_one(Rake::TeamCity::MessageFactory.create_test_output_message(@my_running_example_name, true, stdout_string))
          end
          debug_log("My stdOut: [#{stdout_string}]")
          if (!stderr_string.empty?)
            log_one(Rake::TeamCity::MessageFactory.create_test_output_message(@my_running_example_name, false, stderr_string))
          end
          debug_log("My stdErr: [#{stderr_string}]")
        end
      end
    end
  end
end

at_exit do
  SPEC_FORMATTER_LOG.log_msg("spec formatter.rb: Finished")
  SPEC_FORMATTER_LOG.close
end