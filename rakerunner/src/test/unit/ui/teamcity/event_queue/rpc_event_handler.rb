# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @date: 10.01.2008

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/event_queue/event_handler'
else
  require 'test/unit/ui/teamcity/event_queue/event_handler'
end

module Rake
  module TeamCity

    # Sends data to TeamCity via RPC
    class RPCEventHandler < Logger::EventHandler
      # Name of Teamcity RPC logger method
      TEAMCITY_LOGGER_RPC_NAME = "buildAgent.log"

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
          @server.call(TEAMCITY_LOGGER_RPC_NAME, @buildId, msgs)

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
  end
end