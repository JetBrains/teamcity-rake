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

require "test/unit"
require File.dirname(__FILE__) + '/../src/ext/lib/ruby/1.8/test/unit/teamcity/event_queue/event_queue'

class LoggerTest < Test::Unit::TestCase
  include Rake::TeamCity::MessagesDispather

  def setup
    super()

    @data = []
    start_dispatcher("buildId", "url", 1, 0,
                     Rake::TeamCity::Logger::EventHandler.new do |events, count|
                       for event in events
                         @data << event.data
                         p event.data
                       end                          
                     end)
  end

  def teardown
    super()
    stop_dispatcher
  end

  def test_log_one
    log_one("msg1\nmsg2")
    assert_equal(["msg1\nmsg2"], @data)
  end

  def test_log_many
    log_many(["msg1\nmsg2", "msg3\nmsg4"])
    assert_equal(["msg1\nmsg2", "msg3\nmsg4"], @data)
  end
end