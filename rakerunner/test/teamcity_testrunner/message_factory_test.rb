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
# @date: 30.05.2007

require File.dirname(__FILE__) + '/../test_helper'

require 'src/test/unit/ui/teamcity/message_factory'

module Rake::TeamCity
  class MessageFactoryTest < Test::Unit::TestCase 

    def test_get_block_type
      assert_equal("$BUILD_PROGRESS$", MessageFactory.get_block_type(nil))
      assert_equal("$BUILD_PROGRESS$", MessageFactory.get_block_type(1))
      assert_equal("$BUILD_PROGRESS$", MessageFactory.get_block_type(:bug))

      assert_equal("$COMPILATION_BLOCK$", MessageFactory.get_block_type(:compilation))
      assert_equal("$BUILD_PROGRESS$", MessageFactory.get_block_type(:progress))
      assert_equal("$TEST_BLOCK$", MessageFactory.get_block_type(:test))
      assert_equal("$TEST_SUITE$", MessageFactory.get_block_type(:test_suite))
      assert_equal("$TARGET_BLOCK$", MessageFactory.get_block_type(:target))
      assert_equal("rakeTask", MessageFactory.get_block_type(:task))
    end

    def test_get_msg_status
      assert_equal(1, MessageFactory.get_msg_status(nil))
      assert_equal(1, MessageFactory.get_msg_status(1))
      assert_equal(1, MessageFactory.get_msg_status(:normal))

      assert_equal(0, MessageFactory.get_msg_status(:unknown))
      assert_equal(2, MessageFactory.get_msg_status(:warning))
      assert_equal(3, MessageFactory.get_msg_status(:failure))
      assert_equal(4, MessageFactory.get_msg_status(:error))
    end

    def test_get_time
      assert_equal(Time.now.to_i, MessageFactory.get_time)
    end
    def test_get_bool_str
      assert_equal("false", MessageFactory.get_bool_str(nil))
      assert_equal("false", MessageFactory.get_bool_str(false))

      assert_equal("true", MessageFactory.get_bool_str(true))
      assert_equal("true", MessageFactory.get_bool_str(1))
    end
  end
end