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

require 'builder'

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/string_ext'
else
  require 'test/unit/ui/teamcity/string_ext'
end

module Rake
  module TeamCity
    module MessageFactory
      #See Class jetbrains.buildServer.messages.DefaultMessagesInfo

      # generic messages
      SOURCE_ID = "DefaultMessage";
      MSG_ERROR = "Error";
      MSG_TEXT = "Text";
      MSG_BUILD_FAILURE_DESCRIPTION = "BuildFailureDescription";
      MSG_BLOCK_START = "BlockStart";
      MSG_BLOCK_END = "BlockEnd";

      #  progress markup
      MSG_PROGRESS_STAGE = "ProgressStage";

      # test unit messages
      MSG_TEST_IGNORED = "TestIgnored";
      MSG_TEST_OUTPUT = "TestOutput";
      MSG_TEST_FAILURE = "TestFailure";

      MSG_BLOCK_TYPES = {
        #      :build => "Build"               # BLOCK_TYPE_BUILD
        :progress => "$BUILD_PROGRESS$",       # BLOCK_TYPE_PROGRESS
        :test => "$TEST_BLOCK$",               # BLOCK_TYPE_TEST
        :test_suite => "$TEST_SUITE$",         # BLOCK_TYPE_TEST_SUITE
        :compilation => "$COMPILATION_BLOCK$", # BLOCK_TYPE_COMPILATION
        :target => "$TARGET_BLOCK$",           # BLOCK_TYPE_TARGET
        :task => "rakeTask"
      }

      MSG_STATUS = {
        :unknown => 0,
        :normal => 1,
        :warning => 2,
        :failure => 3,
        :error => 4
      }

      TRUE = "true"
      FALSE = "false"

      # public static BuildMessage1 createTestBlockStart(String blockName)
      # public static BuildMessage1 createBlockStart(final String blockName, final String blockType)
      # public static BuildMessage1 createBlockStart(final String blockName, final String blockType, final Date timestamp)
      # public static BuildMessage1 createTestSuiteStart(String blockName)
      def self.create_open_block(block_name, block_type, msg_status = :normal)
        _create_stub(msg_status, "BlockStart") do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.BlockData") do
            x.blockName   block_name
            x.blockType   get_block_type(block_type)
          end
        end
      end

      # public static BuildMessage1 createTestBlockEnd(String blockName)
      # public static BuildMessage1 createBlockEnd(final String blockName, final String blockType, final Date timestamp)
      # public static BuildMessage1 createBlockEnd(final String blockName, final String blockType)
      # public static BuildMessage1 createTestSuiteEnd(String blockName)
      def self.create_close_block(msg, block_type, msg_status = :normal)
        _create_stub(msg_status, "BlockEnd") do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.BlockData") do
            x.blockName   msg
            x.blockType   get_block_type(block_type)
          end
        end
      end

      def self.create_progress_message(msg, msg_status)
        _create_stub(msg_status, "ProgressStage") do |x|
          x.myValue(msg, "class" => "java.lang.String")
        end
      end

      #  public static BuildMessage1 createTextMessage(final String message, final Status status)
      # public static BuildMessage1 createTextMessage(final String message)
      def self.create_message(msg, msg_status = :normal)
        _create_stub(msg_status, "Text") do |x|
          x.myValue(msg, "class" => "java.lang.String")
        end
      end

      #  public static BuildMessage1 createError(final Throwable throwable, Status status)
      #  public static BuildMessage1 createError(final Throwable throwable)
      def self.create_error_message(msg, stack_trace)
        _create_stub(:error, "Error") do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.ErrorData") do
            x.stackTrace stack_trace
            x.localizedMessage msg
          end
        end
      end

      # public static BuildMessage1 createBuildFailureDescription(final String message)
      def self.create_build_failure_message(msg)
        _create_stub(:failure, "BuildFailureDescription") do |x|
          x.myValue(msg, "class" => "jetbrains.buildServer.messages.ErrorData")
        end
      end

      # Test stderr / stdout data
      # if *is_std_out* is true then stdout data else stderr data
      #
      # public static BuildMessage1 createTestStderr(final String testName, final String output) {
      # public static BuildMessage1 createTestStdout(final String testName, final String output)
      def self.create_test_output_message(test_name, is_std_out, output)
        _create_stub(:normal, "TestOutput") do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.TestOutputData") do
            x.testName test_name
            x.isStdOut get_bool_str(is_std_out)
            x.text output
          end
        end
      end

      # public static BuildMessage1 createTestIgnoreMessage(final String testName, final String reason)
      def self.create_test_ignored_message(msg, test_name)
        _create_stub(:normal, "TestIgnored") do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.IgnoredTestData") do
            x.testName test_name
            x.ignoreReason msg
          end
        end
      end

      #  public static BuildMessage1 createComparisonFailed(final String testName, final Throwable th, String expected, String actual)
      def self.create_comparision_failed_message(test_name, msg, stack_trace, expected_text, actual_text)
        _create_stub(:failure, MSG_TEST_FAILURE) do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.ComparisonFailedData") do
            x.testName test_name
            x.stackTrace stack_trace
            x.localizedMessage msg

            x.expected expected_text
            x.actual actual_text
          end
        end
      end

      # public static BuildMessage1 createTestFailure(final String testName, String message, final String stackTrace)
      # public static BuildMessage1 createTestFailure(final String testName, final Throwable th)
      def self.create_test_problem_message(test_name, msg, stack_trace)
        _create_stub(:failure, "TestFailure") do |x|
          x.myValue("class" => "jetbrains.buildServer.messages.TestProblemData") do
            x.testName test_name
            x.stackTrace stack_trace
            x.localizedMessage msg
          end
        end
      end

      def self.create_user_message(msg, msg_status = :normal)
        type = msg.strip
        if type.starts_with? "##["
          create_open_block type.substring(3), :progress, msg_status
        elsif type.starts_with? "##]"
          create_close_block type.substring(3), :progress, msg_status
        elsif type.starts_with? "##"
          create_progress_message type.substring(2), msg_status
        else
          create_message(msg, msg_status)
        end
      end

      # public static BuildMessage1 createCompilationBlockEnd(String blockName)
      # public static BuildMessage1 createCompilationBlockStart(String blockName)
      # N/A
      private

      def self.get_block_type(type)
        MSG_BLOCK_TYPES[type] || MSG_BLOCK_TYPES[:progress]
      end

      def self.get_msg_status(status)
        MSG_STATUS[status] || MSG_STATUS[:normal]
      end

      def self.get_time
        Time.now.to_i
      end

      def self.get_bool_str(value)
        value ? TRUE : FALSE;
      end

      # Creates XML stub in TeamCity message format
      # <i>msg_status</i> - one of
      # <ul> <li> :unknown
      #   <li> :normal
      #   <li> :waring
      #   <li> :failure
      #   <li> :error </ul>
      # <i>msg_type_id</i> - string
      def self._create_stub(msg_status, msg_type_id)
        x = Builder::XmlMarkup.new(:indent => 3)
        x.tag!("jetbrains.buildServer.messages.BuildMessage1") do
          x.mySourceId    "DefaultMessage"
          x.myTypeId      msg_type_id
          x.myStatus      get_msg_status(msg_status)
          x.myTimestamp   get_time

          yield x
        end
      end
    end
  end
end
