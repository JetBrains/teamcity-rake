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
# @date: 07.06.2007

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/rakerunner_consts'
  require 'src/utils/logger_util'
  require 'src/utils/send_messages_util'
else
  require 'test/unit/ui/teamcity/rakerunner_consts'
  require 'utils/logger_util'
  require 'utils/send_messages_util'
end

RAKE_EXT_LOG = Rake::TeamCity::Utils::RakeFileLogger.new
RAKE_EXT_LOG.log_msg("rake_ext.rb loaded.")

# For RAKEVERSION =  0.7.3 - 0.8.0
require 'rake'

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/message_factory'
  require 'src/test/unit/ui/teamcity/event_queue/messages_dispatcher'
  require 'src/test/unit/ui/teamcity/std_capture_helper'
else
  require 'test/unit/ui/teamcity/message_factory'
  require 'test/unit/ui/teamcity/event_queue/messages_dispatcher'
  require 'test/unit/ui/teamcity/std_capture_helper'
end
######################################################################
######################################################################
# This file is teamcity extension for Rake API                       #
######################################################################
######################################################################

########## Rake  TeamCityApplication #################################
module Rake
  class TeamCityApplication < Application
    extend Rake::SendMessagesUtil
    extend Rake::TeamCity::StdCaptureHelper

    def initialize
      Rake::TeamCity.msg_dispatcher.start_dispatcher
      Rake::TeamCityApplication.send_create_ruby_flow_message
      begin
        super
      rescue Exception => e
        msg, stacktrace =  Rake::TeamCityApplication.format_exception_msg(e)
        Rake::TeamCityApplication.send_error(msg, stacktrace)

        RAKE_EXT_LOG.log_msg("Rake application initialization erors:\n #{msg}\n #{stacktrace}")
        exit(1)
      else
        RAKE_EXT_LOG.log_msg("Rake application initialized.")
      end
    end

    # Wraps block in pair of teamcity's messages: blockStart, blockEnd.
    # Then executes it. If error occurs method will send information to TeamCity and
    # raise special exception to interrupt process, but prevent futher handling of this exception
    def self.target_exception_handling(block_msg, is_execute = false, additional_message = "")
      show_ivoke_block = Rake.application.options.trace || ENV[TEAMCITY_RAKERUNNER_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED]
      create_block = is_execute || (!@already_invoked && show_ivoke_block)

      block_msg = "#{is_execute ? "Execute" : "Invoke"} #{block_msg}"

      # Log in TeamCity
      Rake::TeamCityApplication.send_open_target(block_msg) if create_block

      show_additional_msg = !is_execute && ENV[TEAMCITY_RAKERUNNER_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED] && !Rake.application.options.trace
      Rake::TeamCityApplication.send_normal_user_message(additional_message) if (additional_message && !additional_message.empty? && show_additional_msg)
      
      # Capture output for execution stage
      if is_execute
        old_out, old_err, new_out, new_err = capture_output_start_external
      end
      # Executes task safely
      begin
        yield
      rescue Rake::ApplicationAbortedException => app_e
        raise
      rescue Exception => exc
        # Log in TeamCity
        Rake::TeamCityApplication.process_exception(exc)
      ensure
        if is_execute
          stdout_string, stderr_string = capture_output_end_external(old_out, old_err, new_out, new_err)

          unless (stdout_string.empty?)
            Rake::TeamCityApplication.send_captured_stdout(stdout_string)
            RAKE_EXT_LOG.log_msg("Task[#{block_msg}] Std Output:\n[#{stdout_string}]")
          end
          unless (stderr_string.empty?)
            Rake::TeamCityApplication.send_captured_stderr(stderr_string)
            RAKE_EXT_LOG.log_msg("Task[#{block_msg}] Std Error:\n[#{stderr_string}]")
          end
        end
        # Log in TeamCity
        Rake::TeamCityApplication.send_close_target(block_msg) if create_block
      end
    end

    # Logs exception in TeamCity and raises special(Rake::ApplicationAbortedException.new)
    # exception to prevent further handling
    #
    # *returns* Exit code
    #
    # *raises*  Rake::ApplicationAbortedExceptionExit if not on top level
    def self.process_exception(exception, on_top_level=false)
      # exit code value, 1 by default.
      exit_code = 1;

      # Check if exception is Rake::ApplicationAbortedException
      if exception.instance_of?(Rake::ApplicationAbortedException)
        exc = exception.inner_exception
        applicationAbortedException = true
      else
        exc = exception
        applicationAbortedException = false
      end

      #Process exception
      case (exc)
      when SystemExit
        # Exit silently with current status
        exit_code = exc.status
      when GetoptLong::InvalidOption
        # Exit Silently
      else
        # Sends exception to buildserver, if exception hasn't been sent early(inside some markup block)
        if (!applicationAbortedException)
          # Send exception in current opened teamcity mark-up block.
          msg, stacktrace = Rake::TeamCityApplication.format_exception_msg(exc)
          Rake::TeamCityApplication.send_error(msg, stacktrace)
        end

        if on_top_level
          # Rake aborted
          Rake::TeamCityApplication.send_error_msg("Rake aborted!")
        end
      end

      # Rerise if not on top level to correctly cloose all parent markup blocks
      if !on_top_level
        # Exception was send to teamcity, now we should
        # raise special exception to prevent further handling
        raise Rake::ApplicationAbortedException, exc
      end
      exit_code
    end

    # Formats exception message and stacktrace according current error representation options
    # Returns error msg and stacktrace
    def self.format_exception_msg(exception, show_trace = true)
      back_trace_msg = "\nStacktrace:\n" + exception.backtrace.join("\n")
      if Rake.application.rakefile
        source_file = exception.backtrace.find {|str| str =~ /#{Rake.application.rakefile}/ }
        stacktrace = back_trace_msg + (source_file ? "\n\nSource: #{source_file}": "") + (show_trace ? "" : "\n(See full trace by running task with --trace option)")
      else
        stacktrace = back_trace_msg
      end
      return "#{exception.class.name}: #{exception.message}", stacktrace
    end

    def run
      exit_code = 0
      begin
        super
      rescue Exception => e
        exit_code = Rake::TeamCityApplication.process_exception(e, true)
      ensure
        exit(exit_code) if (exit_code != 0)
      end
    end
  end

  class ApplicationAbortedException < StandardError
    attr_reader :inner_exception

    def initialize(other_exception)
      @inner_exception = other_exception
    end
  end
end

################  Output extension ############################
(require File.dirname(__FILE__) + '/ext/output_ext') unless (ENV[TEAMCITY_RAKERUNNER_LOG_OUTPUT_HACK_DISABLED_KEY] || ENV[TEAMCITY_RAKERUNNER_LOG_OUTPUT_CAPTURER_DISABLED_KEY])

################  Module extension #############################
class Module
  #Overriding rake_extension of standart API. 0.7.3 - 0.8.0
  def rake_extension(method)
    if instance_methods.include?(method)
      Rake::TeamCityApplication.send_warning("WARNING: Possible conflict with Rake extension: #{self}##{method} already exists")
    else
      yield
    end
  end
end

################  Rake extension #############################
module Rake

  # Rake module singleton methods.
  class << self
    # Current Rake Application: 0.7.3 - 0.8.0
    def application
      @application ||= Rake::TeamCityApplication.new
    end
  end
end

################# Task extnesion #########################################
class Rake::Task
  NEW_API = defined? invoke_with_call_chain

  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Invoke the task if it is needed.  Prerequites are invoked first.
  def my_invoke_with_call_chain(*args)
    Rake::TeamCityApplication.target_exception_handling(name, false, format_trace_flags) do
     method(:standart_invoke_with_call_chain).arity == 0 ? standart_invoke_with_call_chain() : standart_invoke_with_call_chain(args)
    end
  end
  private :my_invoke_with_call_chain
  if NEW_API
    # 0.8.0 and higher
    alias :standart_invoke_with_call_chain :invoke_with_call_chain
    # overrides 'invoke_with_call_chain' with 'my_invoke_with_call_chain'
    alias :invoke_with_call_chain :my_invoke_with_call_chain
  else
    # 0.7.3
    alias :standart_invoke_with_call_chain :invoke
    # overrides 'invoke' with 'my_invoke_with_call_chain'
    alias :invoke :my_invoke_with_call_chain
  end
  private :standart_invoke_with_call_chain
  public :invoke

  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Execute the actions associated with this task.
  alias :standart_execute :execute
  private :standart_execute
  def execute(*args)
    standart_execute_block = Proc.new do
      method(:standart_execute).arity == 0 ? standart_execute() : standart_execute(args)
    end

    if application.options.dryrun
      Rake::TeamCityApplication.target_exception_handling(name, true, "(dry run)", &standart_execute_block)
    else
      Rake::TeamCityApplication.target_exception_handling(name, true, &standart_execute_block)
    end
  end
end

###########  RakeFilesUtils  extenstion ##############################
module RakeFileUtils

  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Use this function to prevent protentially destructive ruby code from
  # running when the :nowrite flag is set.
  #
  # Example:
  #
  #   when_writing("Building Project") do
  #     project.build
  #   end
  #
  # The following code will build the project under normal conditions. If the
  # nowrite(true) flag is set, then the example will print:
  #      DRYRUN: Building Project
  # instead of actually building the project.
  #
  alias standart_when_writing when_writing
  private :standart_when_writing
  def when_writing(msg=nil)
    if RakeFileUtils.nowrite_flag
      Rake::TeamCityApplication.send_info_msg("DRYRUN: #{msg}") if msg
    end
    standart_when_writing(msg)
  end

  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Send the message to the default rake output (which is $stderr).
  def rake_output_message(message)
    Rake::TeamCityApplication.send_error_msg(message)
#    $stderr.puts(message)
  end
end

###########  Rake::Application  extenstion #############################
class Rake::Application
  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Provide standard execption handling for the given block.
  #
  # This method wraps exceptions into  Rake::TeamCityApplication.process_exception exception.
  # Such exceptions will be processed in Rake::TeamCityApplication.run
  def standard_exception_handling
    begin
      yield
    rescue Rake::ApplicationAbortedException => app_e
      raise
    rescue Exception => exc
      # Log in TeamCity
      Rake::TeamCityApplication.process_exception(exc)
    end
  end

  # Overrides standart API.  0.7.3 - 0.8.0
  #
  # Warn about deprecated use of top level constant names.
  def const_warning(const_name)
    @const_warning ||= false
    if !@const_warning
      msg = %{WARNING: Deprecated reference to top-level constant '#{const_name}' } +
            %{found at: #{rakefile_location}} +
            %{    Use --classic-namespace on rake command} +
            %{    or 'require "rake/classic_namespace"' in Rakefile}
      Rake::TeamCityApplication.send_warning(msg)
    end
    @const_warning = true
  end
end

at_exit do
  RAKE_EXT_LOG.log_block("rake_ext : Closing connection...") do
    Rake::TeamCity.msg_dispatcher.stop_dispatcher;
  end
  RAKE_EXT_LOG.log_msg("rak_ext.rb: Finished.");
  RAKE_EXT_LOG.close
end