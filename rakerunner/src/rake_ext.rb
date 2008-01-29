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
else
  require 'test/unit/ui/teamcity/rakerunner_consts'
end

RAKE_EXT_LOG_ENABLED = ENV[TEAMCITY_RAKERUNNER_LOG_PATH_KEY]
if RAKE_EXT_LOG_ENABLED
  RAKE_EXT_LOG = File.new(ENV[TEAMCITY_RAKERUNNER_LOG_PATH_KEY] + "/rakeRunner_rake.log", "a+")
  RAKE_EXT_LOG << "\n[#{Time.now}] : Started\n"
end

# For RAKEVERSION =  0.7.2 - 0.8.0
require 'rake'

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/message_factory'
  require 'src/test/unit/ui/teamcity/event_queue/messages_dispatcher'
else
  require 'test/unit/ui/teamcity/message_factory'
  require 'test/unit/ui/teamcity/event_queue/messages_dispatcher'
end
######################################################################
######################################################################
# This file is teamcity extension for Rake API                       #
######################################################################
######################################################################

########## Rake  TeamCityApplication #################################
# TODO User output from tests
# TODO User output from raketasks

module Rake
  class TeamCityApplication < Application
    attr_reader :server, :build_id_str

    def initialize
      Rake::TeamCity.msg_dispatcher.start_dispatcher

      begin
        super
      rescue Exception => e
        if RAKE_EXT_LOG_ENABLED
          RAKE_EXT_LOG << "\n[#{Time.now}] : Rake application initialization erors:\n #{msg}\n #{stacktrace}\n"
        end

        msg, stacktrace =  Rake::TeamCityApplication.format_exception_msg(e, options.trace)
        Rake::TeamCityApplication.send_error(msg, stacktrace)

        #Rake::TeamCity.msg_dispatcher.stop_dispatcher(true)  - will be closed at_exit
        exit(1)
      else
        if RAKE_EXT_LOG_ENABLED
          RAKE_EXT_LOG << "\n[#{Time.now}] : Rake application initialized.\n"
        end
      end
    end

    def self.send_error(msg, stacktrace)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_error_message(msg, stacktrace)}
    end

    def self.send_warning(msg)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg, :warning)}
    end

    def self.send_info_msg(msg)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg)}
    end

    def self.send_error_msg(msg)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg, :error)}
    end

    def self.send_open_target(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_open_block(msg, :target)}
    end

    def self.send_close_target(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_close_block(msg, :target)}
    end

    def self.send_noraml_user_message(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_user_message(msg)}
    end

    def self.send_xml_to_teamcity
      Rake::TeamCity.msg_dispatcher.log_one(yield)
    end

    # Wraps block in pair of teamcity's messages: blockStart, blockEnd.
    # Then executes it. If error occurs method will send information to TeamCity and
    # raise special exception to interrupt process, but prevent futher handling of this exception
    def self.target_exception_handling(block_msg)
      # Log in TeamCity
      Rake::TeamCityApplication.send_open_target(block_msg) if Rake.application.options.trace

      # Executes task safely
      begin
        yield
      rescue Rake::ApplicationAbortedException => app_e
        raise
      rescue Exception => exc
        # Log in TeamCity
        Rake::TeamCityApplication.process_exception(exc)
      ensure
        # Log in TeamCity
        Rake::TeamCityApplication.send_close_target(block_msg) if Rake.application.options.trace
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
        break;
      when GetoptLong::InvalidOption
        # Exit Silently
        break;
      else
        # Sends exception to buildserver, if exception hasn't been sent early(inside some markup block)
        if (!applicationAbortedException)
          # Send exception in current opened teamcity mark-up block.
          trace = Rake.application.options.trace
          msg, stacktrace = Rake::TeamCityApplication.format_exception_msg(exc, trace)
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
        raise Rake::ApplicationAbortedException.new(exc)
      end
      exit_code
    end

    # Formats exception message and stacktrace according current error representation options
    # Returns error msg and stacktrace
    def self.format_exception_msg(exception, show_trace = false)
      if show_trace
        stacktrace = "\nStacktrace:\n" + exception.backtrace.join("\n")
      elsif Rake.application.rakefile
        stacktrace = "\nSource: #{exception.backtrace.find {|str| str =~ /#{Rake.application.rakefile}/ }|| ""}\n(See full trace by running task with --trace)"
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

#############  Object extension #############################
class Object
  def puts(obj)
    Rake::TeamCityApplication.send_noraml_user_message(obj.to_s)
  end

  def printf(s, *args)
    puts(sprintf(s, *args))
  end
end

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

  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Invoke the task if it is needed.  Prerequites are invoked first.
  alias standart_invoke invoke
  private :standart_invoke
  def invoke(*args)
    Rake::TeamCityApplication.target_exception_handling("Invoke #{name} #{format_trace_flags}") do
     method(:standart_invoke).arity == 0 ? standart_invoke() : standart_invoke(args)
    end
  end

  # Overrides standart API. 0.7.3 - 0.8.0
  #
  # Execute the actions associated with this task.
  alias standart_execute execute
  private :standart_execute
  def execute(*args)
    standart_execute_block = Proc.new do
      method(:standart_execute).arity == 0 ? standart_execute() : standart_execute(args)
    end

    if application.options.dryrun
      Rake::TeamCityApplication.target_exception_handling("Execute (dry run) #{name}", &standart_execute_block)
    else
      Rake::TeamCityApplication.target_exception_handling("Execute #{name}", &standart_execute_block)
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
  Rake::TeamCity.msg_dispatcher.stop_dispatcher(true);
  if RAKE_EXT_LOG_ENABLED
    RAKE_EXT_LOG << "[#{Time.now}] : Closing connection....\n";
    RAKE_EXT_LOG << "[#{Time.now}] : Closed.\n";

    RAKE_EXT_LOG << "[#{Time.now}] : Finished\n\n";
    RAKE_EXT_LOG.close
  end
end