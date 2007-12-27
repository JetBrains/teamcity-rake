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
# @date: 08.06.2007

require 'test/unit'
require 'test/unit/ui/teamcity/event_queue/event_queue'

#################### Extensions #######################################

module XMLRPC

  class Client
    attr_reader :host
    attr_reader :port
    attr_reader :path
  end
end

#######################################################################
#######################################################################
###################    Tests     ######################################
#######################################################################
#######################################################################

  class MessagesDispather < Test::Unit::TestCase

    def test_log_one_not_running
      ENV['idea.build.server.build.id'] = "build_id"
      ENV['idea.build.agent.port'] = '777'

      assert_raise(Rake::TeamCity::ConnectionException, "Dispatcher isn't running. At first start it.") do
        Rake::TeamCity.msg_dispatcher.log_one 'bugaga'
      end
    end

    def test_log_many_not_running
      ENV['idea.build.server.build.id'] = "build_id"
      ENV['idea.build.agent.port'] = '777'

      assert_raise(Rake::TeamCity::ConnectionException, "Dispatcher isn't running. At first start it.") do
        Rake::TeamCity.msg_dispatcher.log_many ['bugaga']
      end
    end


    def test_get_teamcity_connection_params_ok
      ENV["idea.build.server.build.id"] = "build_777";
      ENV["idea.build.agent.port"] = "777";

      server, build_id = Rake::TeamCity::MessagesDispather.get_teamcity_connection_params
      assert_equal "build_777", build_id

      assert_not_nil server
      assert_equal "localhost", server.host
      assert_equal 777, server.port
      assert_equal "/RPC2", server.path
    end

    def test_get_teamcity_connection_params_fail
      ENV["idea.build.server.build.id"] = nil;
      ENV["idea.build.agent.port"] =  nil;

      assert_raise(Rake::TeamCity::ConnectionException, "Rake::TeamCityException: Can't connect to agent. Wrong parameters:  buildId=nil, port=0") do
        server, build_id = Rake::TeamCity::MessagesDispather.get_teamcity_connection_params
      end
    end

    #TODO why doesn't run by teamcity
    def test_get_teamcity_connection_params_wrong_type
      ENV["idea.build.server.build.id"] = "build_id";
      ENV["idea.build.agent.port"] =  "foo"

      server, build_id = Rake::TeamCity::MessagesDispather.get_teamcity_connection_params
      assert_equal "build_id", build_id

      assert_not_nil server
      assert_equal "localhost", server.host
      assert_equal 0, server.port
      assert_equal "/RPC2", server.path
    end

    def test_msg_send_failed
      assert_nil(1)
    end

    #  def test_msg_send_failed
    #    ENV["idea.build.server.build.id"] = "build_id";
    #    ENV["idea.build.agent.port"] =  "foo"
    #
    #    server, build_id = TeamCity.msg_dispatcher.start_dispatcher(2, 0.1)
    #    def server.call(method, *args)
    #      fail
    #    end
    #
    #    TeamCity.msg_dispatcher.log_one("msg1")
    #    TeamCity.msg_dispatcher.stop_dispatcher(true)
    #
    #    assert_raise() do
    #
    #    end
    #  end

    def test_msg_send
      ENV["idea.build.server.build.id"] = "build_id";
      ENV["idea.build.agent.port"] =  "foo"

      server, build_id = Rake::TeamCity::MessagesDispather.get_teamcity_connection_params

      @data = []
      handler = Rake::TeamCity::Logger::EventHandler.new  do |events, count|
        for event in events
          @data << event.data
        end
      end

      send_msgs(handler) do
        Rake::TeamCity.msg_dispatcher.log_one("msg1")
        Rake::TeamCity.msg_dispatcher.log_one("msg2")
      end
      assert_equal(["msg1", "msg2"], @data)

      send_msgs(handler) do
        Rake::TeamCity.msg_dispatcher.log_many(["msg1", "msg2"])
        Rake::TeamCity.msg_dispatcher.log_many(["msg3", "msg4"])
      end
      assert_equal(["msg1", "msg2", "msg3", "msg4"], @data)

      send_msgs(handler) do
        Rake::TeamCity.msg_dispatcher.log_one("msg1")
        Rake::TeamCity.msg_dispatcher.log_many(["msg2", "msg3"])
        Rake::TeamCity.msg_dispatcher.log_one("msg4")
      end
      assert_equal(["msg1", "msg2", "msg3", "msg4"], @data)
    end

    private
    def send_msgs(handler)
      @data = []
      disp = Rake::TeamCity::MessagesDispather.new
      disp.start_dispatcher(2, 0.1, handler)
      yield
      disp.stop_dispatcher(true)
    end
  end

#TODO test start dispatcher