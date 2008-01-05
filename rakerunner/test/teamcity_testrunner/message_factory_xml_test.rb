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
# @date: 03.05.2007

require File.dirname(__FILE__) + '/../test_helper'

require 'src/test/unit/ui/teamcity/message_factory'


module Rake::TeamCity::MessageFactory
  def rescue_action(e)
    raise e
  end;

  def self.get_time
    "TIME"
  end
  def self.get_block_type(type)
    "TYPE"
  end
  def self.get_msg_status(status)
    "MSG_ST"
  end

  def self.get_bool_str(value)
    "BOOL"
  end
end

module Rake::TeamCity
  class MessageFactoryTest < Test::Unit::TestCase
    def test_create_open_block
      msg = <<-STRING
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>BlockStart</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.BlockData\">
      <blockName>name</blockName>
      <blockType>TYPE</blockType>
   </myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING

      assert_equal(msg, MessageFactory.create_open_block("name", nil, nil))
      assert_equal(msg, MessageFactory.create_open_block("name", nil))
    end

    def test_create_close_block
      msg = <<-STRING
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>BlockEnd</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.BlockData\">
      <blockName>name</blockName>
      <blockType>TYPE</blockType>
   </myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING

      assert_equal(msg, MessageFactory.create_close_block("name", nil, nil))
      assert_equal(msg, MessageFactory.create_close_block("name", nil))
    end

    def test_create_progress_message
      assert_equal(<<-STRING, MessageFactory.create_progress_message("name", nil))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>ProgressStage</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"java.lang.String\">name</myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_message
      assert_equal(<<-STRING, MessageFactory.create_message("name", nil))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>Text</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"java.lang.String\">name</myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_error_message
      assert_equal(<<-STRING, MessageFactory.create_error_message("name", nil))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>Error</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.ErrorData\">
      <stackTrace></stackTrace>
      <localizedMessage>name</localizedMessage>
   </myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_build_failure_message
      assert_equal(<<-STRING, MessageFactory.create_build_failure_message("name"))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>BuildFailureDescription</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.ErrorData\">name</myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_test_ouptut_message
      assert_equal(<<-STRING, MessageFactory.create_test_ouptut_message("name", nil, nil, nil))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>TestOutput</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.TestOutputData\">
      <testName></testName>
      <isStdOut>BOOL</isStdOut>
      <text></text>
   </myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_test_ignored_message
      assert_equal(<<-STRING, MessageFactory.create_test_ignored_message("name", nil))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>TestIgnored</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.IgnoredTestData\">
      <testName></testName>
      <ignoreReason>name</ignoreReason>
   </myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_test_problem_message
      assert_equal(<<-STRING, MessageFactory.create_test_problem_message("name", "msg", nil))
<jetbrains.buildServer.messages.BuildMessage1>
   <mySourceId>DefaultMessage</mySourceId>
   <myTypeId>TestFailure</myTypeId>
   <myStatus>MSG_ST</myStatus>
   <myTimestamp>TIME</myTimestamp>
   <myValue class=\"jetbrains.buildServer.messages.TestProblemData\">
      <testName>name</testName>
      <stackTrace></stackTrace>
      <localizedMessage>msg</localizedMessage>
   </myValue>
</jetbrains.buildServer.messages.BuildMessage1>
      STRING
    end

    def test_create_user_message
      assert_equal(MessageFactory.create_message("name", nil),
      MessageFactory.create_user_message("name", nil))

      assert_equal(MessageFactory.create_open_block("name", :progress, nil),
      MessageFactory.create_user_message("##[name", nil))

      assert_equal(MessageFactory.create_close_block("name", :progress, nil),
      MessageFactory.create_user_message("##]name", nil))

      assert_equal(MessageFactory.create_progress_message("name", nil),
      MessageFactory.create_user_message("##name", nil))
    end

    def test_mock
      assert_equal("TIME", MessageFactory.get_time)
      assert_equal("TYPE", MessageFactory.get_block_type(nil))
      assert_equal("MSG_ST", MessageFactory.get_msg_status(nil))
      assert_equal("BOOL", MessageFactory.get_bool_str(nil))
    end
  end
end