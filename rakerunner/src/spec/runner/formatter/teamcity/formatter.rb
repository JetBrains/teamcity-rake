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
          block = Proc.new {Rake::TeamCity.msg_dispatcher.log_one(msg)}
          ENV[TEAMCITY_RAKERUNNER_LOG_RSPEC_XML_MSFS_KEY] ? SPEC_FORMATTER_LOG.log_block(msg.hash, msg, block) : block.call
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

        #########################################################
        #########################################################
         # start / fail /pass /pending method
        #########################################################
        #########################################################
        @@RUNNING_EXAMPLES_STORAGE = {}

        #TODO: Re implement this redundant protection, when corresponding bug in RSpec 1.1.3 - .. will be fixed
        #Sometimes example_started is executed in another group
        #Such behavior leds to inconsistent order of exaple_started, example_passed/failed/pending events
        #This hack helps in this problem:
        # *Output start capture at example started
        # *Example start/passed/failed/pending methods shares example's full and output files in map
        #  if several tasks are executed in one moment they will have different Flow_id_suffixes
        #
        # In fact this is a HACK
        def example_started(example)
          debug_log("example started [#{example.description}]  #{example}")

          my_running_example_desc = example.description
          my_running_example_full_name = "#{@example_group_desc} #{my_running_example_desc}"

          # Send open event
          # * additional_flowid_suffix - uses for concurrent examples running, due to rspec bug
          if @@RUNNING_EXAMPLES_STORAGE.empty?
            additional_flowid_suffix = ""
          else
            additional_flowid_suffix = example.hash.to_s;
            debug_log("New flowid was creating [#{Rake::TeamCity::MessageFactory::RAKE_FLOW_ID+additional_flowid_suffix}]...")
            log_one(Rake::TeamCity::MessageFactory.create_flow_message(Rake::TeamCity::MessageFactory::RAKE_FLOW_ID, "", additional_flowid_suffix))
          end
          debug_log("Example starting.. - full name = [#{my_running_example_full_name}], addit flowid suffix=[#{additional_flowid_suffix}], desc = [#{my_running_example_desc}]")
          log_one(Rake::TeamCity::MessageFactory.create_open_block(my_running_example_full_name, :test, additional_flowid_suffix))

          std_files = capture_output_start_external
          debug_log("Output caputre started.")
          @@RUNNING_EXAMPLES_STORAGE[example] = RunningExampleData.new(my_running_example_full_name, additional_flowid_suffix, *std_files)
        end


        def example_passed(example)
          debug_log("example_passed[#{example.description}]  #{example}")

          stop_capture_output_and_log_it(example)

          close_test_block(example)
        end

        def example_failed(example, counter, failure)
          debug_log("example failed[#{example.description}]  #{example}")

          stop_capture_output_and_log_it(example)

          # example service data
          example_data = @@RUNNING_EXAMPLES_STORAGE[example]
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          message =  failure.exception.nil? ? "[Without Exception]" : "#{failure.exception.class.name}: #{failure.exception.message}"
          backtrace = failure.exception.nil? ? "" : format_backtrace(failure.exception.backtrace)

          # failure description
          full_failure_description = message
          (full_failure_description += "\n\n    " + backtrace) if backtrace

          debug_log("Example failing... full name = [#{running_example_full_name}], Message:\n#{message} \n\nBackrace:\n#{backtrace}\n\nFull failure desc:\n#{full_failure_description}\n, additional flowid suffix=[#{additional_flowid_suffix}]")
          log_one(Rake::TeamCity::MessageFactory.create_test_problem_message(running_example_full_name, message, full_failure_description, additional_flowid_suffix))

          close_test_block(example)
        end

        # HACK for support RSPEC API changes, see  svn: http://rspec.rubyforge.org/svn/trunk, rev. #3305
        def example_pending(*args)
          method_arity = args.length
          if method_arity == 2
            # rev. #3305 changes
            example_group_desc, example, message = nil, *args
          elsif method_arity == 3
            # old API
            example_group_desc, example, message = args
          else
             log_and_raise_internal_error "TeamCity Ruby plugin ins't compatible with this RSpec version.\n BaseFormatter.example_pendin arity = #{method_arity}."
          end
          example_pending_3args(example_group_desc, example, message)
        end

        # example_group_desc - can be nil
        def example_pending_3args(example_group_desc, example, message)

          debug_log("pending: #{example_group_desc}, #{example.description}, #{message}, #{example}")
          if example_group_desc
            #Old API, 1.1.3
            assert_example_group_valid(example_group_desc)
          end

          stop_capture_output_and_log_it(example)

          # example service data
          example_data = @@RUNNING_EXAMPLES_STORAGE[example]
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name
   
          debug_log("Example pending... [#{@example_group_desc}].[#{running_example_full_name}] - #{message}, additional flowid suffix=[#{additional_flowid_suffix}]")
          log_one(Rake::TeamCity::MessageFactory.create_test_ignored_message(message, running_example_full_name,additional_flowid_suffix))

          close_test_block(example)
        end

# TODO see snippet_extractor.rb
# Here we can add file link or show code lined
#        def extra_failure_content(failure)
#          # @snippet_extractor.snippet(failure.exception)
#        end

        def dump_summary(duration, example_count, failure_count, pending_count)
          if dry_run?
            totals = "This was a dry-run"
          else
            totals = "#{example_count} example#{'s' unless example_count == 1}, #{failure_count} failure#{'s' unless failure_count == 1}, #{example_count - failure_count - pending_count} passed"
            totals << ", #{pending_count} pending" if pending_count > 0
          end

          close_example_group

          # Total statistic
          debug_log(totals)
          log_one(Rake::TeamCity::MessageFactory.create_message(totals))

          # Time statistic from Spec Runner
          status_message = "Finished in #{duration} seconds"
          debug_log(status_message)
          log_one(Rake::TeamCity::MessageFactory.create_progress_message(status_message))

          unless @@RUNNING_EXAMPLES_STORAGE.empty?
            # unfinished examples statistics
            msg = "TeamCity Ruby plugin ins't compatible with this RSpec version.\n Following examples weren't finished:"
            @@RUNNING_EXAMPLES_STORAGE.each { |key, value|
              msg += "\n#{value.full_name}"
            }
            log_and_raise_internal_error msg
          end

          # close xml-rpc connection
          debug_log("Closing dispatcher..")
          @msg_dispatcher.stop_dispatcher if @manual_start
        end

        ###########################################################################
        ###########################################################################
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
          debug_log("Adding example group(behaviour)...: [#{@example_group_desc}]...")
          log_one(Rake::TeamCity::MessageFactory.create_open_block(@example_group_desc, :test_suite))
        end


        def close_test_block(example)
          example_data = @@RUNNING_EXAMPLES_STORAGE.delete(example)
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          debug_log("Example finishing... full example name = [#{running_example_full_name}], additional flowid suffix=[#{additional_flowid_suffix}]")
          log_one(Rake::TeamCity::MessageFactory.create_close_block(running_example_full_name, :test, additional_flowid_suffix))
        end

        def close_example_group
          debug_log("Closing example group(behaviour): [#{@example_group_desc}].")
          log_one(Rake::TeamCity::MessageFactory.create_close_block(@example_group_desc, :test_suite))
        end

        def debug_log(string)
          # Logs output.
          SPEC_FORMATTER_LOG.log_msg(string)
        end

        def stop_capture_output_and_log_it(example)
          example_data = @@RUNNING_EXAMPLES_STORAGE[example]
          additional_flowid_suffix = example_data.additional_flowid_suffix
          running_example_full_name = example_data.full_name

          stdout_string, stderr_string = capture_output_end_external(*example_data.get_std_files)
          debug_log("Example capturing was stopped.")

          debug_log("My stdOut: [#{stdout_string}] additional flow id=[#{additional_flowid_suffix}]")
          if (!stdout_string.empty?)
            log_one(Rake::TeamCity::MessageFactory.create_test_output_message(running_example_full_name, true, stdout_string, additional_flowid_suffix))
          end
          debug_log("My stdErr: [#{stderr_string}] additional flow id=[#{additional_flowid_suffix}]")
          if (!stderr_string.empty?)
            log_one(Rake::TeamCity::MessageFactory.create_test_output_message(running_example_full_name, false, stderr_string, additional_flowid_suffix))
          end
        end

        ######################################################
        ############# Assertions #############################
        ######################################################
#        def assert_example_valid(example_desc)
#           if (example_desc != @my_running_example_desc)
#              msg = "Example [#{example_desc}] doesn't correspond to current running example [#{@my_running_example_desc}]!"
#              debug_log(msg)
#              ... [send error to teamcity] ...
#              close_test_block
#
#              raise Rake::TeamCity::InnerException, msg, caller
#            end
#        end

        # We doesn't support concurrent example groupd executing
        def assert_example_group_valid(example_group_desc)
           if (example_group_desc !=  @example_group_desc)
              msg = "Example group(behaviour) [#{example_group_desc}] doesn't correspond to current running example group [#{ @example_group_desc}]!"
              debug_log(msg)
              close_test_block

              raise Rake::TeamCity::InnerException, msg, caller
            end
        end
        ######################################################
        def log_and_raise_internal_error(msg)
          debug_log(msg)
          log_one(Rake::TeamCity::MessageFactory.create_error_message(msg, ""))

          raise Rake::TeamCity::InnerException, msg, caller
        end
        ######################################################
        class RunningExampleData
          attr_reader :full_name # full task name, example name in build log
          attr_reader :additional_flowid_suffix # to support concurrently running examples
          attr_reader :stdout_file_old # before capture
          attr_reader :stderr_file_old # before capture
          attr_reader :stdout_file_new  #current capturing storage
          attr_reader :stderr_file_new # current capturing storage

          def initialize(full_name, additional_flowid_suffix, stdout_file_old, stderr_file_old, stdout_file_new, stderr_file_new)
            @full_name = full_name
            @additional_flowid_suffix = additional_flowid_suffix
            @stdout_file_old = stdout_file_old
            @stderr_file_old = stderr_file_old
            @stdout_file_new = stdout_file_new
            @stderr_file_new = stderr_file_new
          end

          def get_std_files
            return @stdout_file_old, @stderr_file_old, @stdout_file_new, @stderr_file_new
          end

          def debug_log(string)
            # Logs output.
            SPEC_FORMATTER_LOG.log_msg(string)
          end
        end
      end
    end
  end
end

at_exit do
  SPEC_FORMATTER_LOG.log_msg("spec formatter.rb: Finished")
  SPEC_FORMATTER_LOG.close
end