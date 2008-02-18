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
# @date: 10.01.2008
if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/rakerunner_consts'
  require 'src/utils/logger_util'
  require 'src/test/unit/ui/teamcity/event_queue/event_handler'
  require 'src/test/unit/ui/teamcity/event_queue/messages_dispatcher'
else
  require 'test/unit/ui/teamcity/rakerunner_consts'
  require 'utils/logger_util'
  require 'test/unit/ui/teamcity/event_queue/event_handler'
  require 'test/unit/ui/teamcity/event_queue/messages_dispatcher'
end

RPC_EVENT_HANDLER_LOG = Rake::TeamCity::Utils::RPCMessagesLogger.new
RPC_EVENT_HANDLER_LOG.log_msg("rpc_event_handler.rb loaded.", true)

module Rake
  module TeamCity

    # Sends data to TeamCity via RPC
    class RPCEventHandler < Logger::EventHandler
      # Creates a RPCEventHandler
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
        RPC_EVENT_HANDLER_LOG.log_msg("RPCEventHadler initialized.", true)
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
          RPC_EVENT_HANDLER_LOG.log_block(events.hash,  msgs, true) do
            @server.call(TEAMCITY_LOGGER_RPC_NAME, @buildId, msgs)
          end
        rescue XMLRPC::FaultException => e
          # Retrying...
          RPC_EVENT_HANDLER_LOG.log_msg("[h#{events.hash}]: Sending failed! Retrying...[#{count}] #{e.class.name}:#{e.message}", true)
          if count < @max_attemps
            sleep @retry_delay
            process(events, count + 1)
          end
          RPC_EVENT_HANDLER_LOG.log_msg("[h#{events.hash}]: Successful retry.", true)
        rescue Exception => e1
          RPC_EVENT_HANDLER_LOG.log_msg("[h#{events.hash}]: Sending failed! Exception #{e1.class.name}:#{e1.message}", true)
          raise Rake::TeamCity::ConnectionException, "Failed: Can't send messages to server\n#{e1}"
        end
      end
    end
  end
end

at_exit do
  RPC_EVENT_HANDLER_LOG.log_block("Stop dispatcher.. Thread=[#{Thread.current}]", nil, true) do
    Rake::TeamCity.msg_dispatcher.stop_dispatcher(true);
  end

  RPC_EVENT_HANDLER_LOG.log_msg("rpc_event_handler finished. Thread=[#{Thread.current}]", true);
  RPC_EVENT_HANDLER_LOG.close
end