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

require "xmlrpc/client"

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/event_queue/events_dispatcher'
  require 'src/test/unit/ui/teamcity/event_queue/event_handler'
  require 'src/test/unit/ui/teamcity/event_queue/event'
else
  require 'test/unit/ui/teamcity/event_queue/events_dispatcher'
  require 'test/unit/ui/teamcity/event_queue/event_handler'
  require 'test/unit/ui/teamcity/event_queue/event'
end

# Dispatches messages to TeamCity buildserver
module Rake
  module TeamCity
    class << self
      def msg_dispatcher
        @msg_dispatcher ||= MessagesDispather.new
      end
    end

    class MessagesDispather
      include Logger

      # Check does Teamcity test runner is enabled 
      def self.teamcity_test_runner_enabled_set?
        ENV['idea.build.server.build.id'] && ENV['idea.build.agent.port']
      end

      # Creates connection XMLRPC::Client to TeamCity server.
      # Uses enviroment vriables ENV['idea.build.server.build.id'] and ENV['idea.build.agent.port'].
      #
      # <b> Returns: </b> connection object and build_id string
      # <b> Raise: </b> ConnectionException if params are not valid
      def self.get_teamcity_connection_params
        build_id_str = ENV['idea.build.server.build.id']
        begin
          port_int = ENV['idea.build.agent.port'].to_i
        ensure
          if !build_id_str or !port_int
            fail ConnectionException.new("Can't connect to agent. Wrong parameters:  buildId=#{build_id_str || "nil"}, port=#{port_int || "nil"}")
          end
        end
        server = XMLRPC::Client.new("localhost", "/RPC2", port_int)
        return server, build_id_str
      end

      # Creates connection to TeamCity and starts dispatcher.
      # Do nothing, if it has been already started.
      #
      # max_attemps - max attemps count for message resending
      # retry_delay - rational number, delay beetween attemps in seconds
      # handler - this handler will process events, by default it is SendDataEventHandler
      #
      # <b> Returns </b> server(XMLRPC::Client object), build_id_str(build id from teamcity, is used for autorization)
      # <b> Raise: </b> ConnectionException if params are not valid
      def start_dispatcher(max_attemps = 100, retry_delay = 0.25, handler = nil)

        unless (started?)
          @server, @build_id_str = MessagesDispather.get_teamcity_connection_params
          unless handler
            handler = SendDataEventHandler.new(@build_id_str, @server, max_attemps, retry_delay)
          end
          @dispatcher = EventsDispatcher.new(handler)
          @dispatcher.start
        else
          new_server, new_build_id_str = MessagesDispather.get_teamcity_connection_params
          if (new_build_id_str != @build_id_str)
            raise ConnectionException.new("Attemp to start dispatcher for other build_id when another dispatcher is running. At first stop previous one.")            
          end

        end

        return @server, @build_id_str
      end

      # Stops new events dispatching, and waiting while event queue will finish
      # <b>join_thread</b> - if true, current thread will wait dispatcher for completing event dispatching. You may use it in tests.
      def stop_dispatcher(join_thread = false)
        if started?
          @dispatcher.stop(join_thread)
          @dispatcher = nil
        end
      end

      # Sends to TeamCity array of messages.
      # May throw ConnectionException
      def log_many(messages)
        for msg in messages
          log_one(msg)
        end
      end

      # Sends to TeamCity message.
      # May throw ConnectionException
      def log_one(msg)
        event = Logger::Event.new(self, msg)

        raise ConnectionException.new("Dispatcher isn't running. At first start it.") unless started?
        @dispatcher.dispatch event
      end

      def started?
        @dispatcher != nil
      end
    end


    # Sends data to TeamCity via RPC
    class SendDataEventHandler < Logger::EventHandler

      # Creates a SendDataEventHandler
      #
      # buildId - build id from teamcity, is used for autorization
      # server - XMLRPC::Client object, not nil.
      # max_attemps - max attemps count for message resending
      # retry_delay - rational number, delay beetween attemps in seconds
      #
      def initialize(buildId, server, max_attemps, retry_delay)
        @buildId = buildId
        @server = server
        @max_attemps = max_attemps
        @retry_delay = retry_delay
      end

      # Sends msg to TeamCity buildserver using RPC
      #
      # events - Array of events, e.g. TeamCity::Logger::Event
      # count - attemp number, if count < @max_attemps program will retry sending
      #
      def process(events, count = 0)
        begin
          # Collect messages
          msgs = []
          for event in events
            msgs << event.data
          end

          # Sending
          @server.call("buildAgent.log", @buildId, msgs)

        rescue XMLRPC::FaultException => e
          # Retrying...
          if count < @max_attemps
            sleep @retry_delay
            process(events, count + 1)
          end
        rescue Exception => e1
          raise ConnectionException.new("Failed: Can't send messages to server\n#{e1}")
        end
      end
    end

    class ConnectionException < StandardError
      def initialize(msg = nil)
        super(msg)
      end
    end
  end
end