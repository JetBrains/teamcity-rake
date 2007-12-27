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

require 'test/unit/ui/teamcity/event_queue/event'

module Rake
  module TeamCity
    module Logger

      class EventsProcessor
        def initialize(queue, events_sequence_handler, exceptions_handler)
          @queue = queue
          @events_sequence_handler = events_sequence_handler
          @exceptions_handler = exceptions_handler
        end

        def start
          @is_running = true
          @should_stop = false

          @processor_thread = Thread.new do
            while (@is_running && !@should_stop)
              process_events
            end
            process_events
          end
        end

        def process_events
          events = []

          until @queue.empty? do
            events << @queue.pop
          end

          unless events.empty?
            begin
              @events_sequence_handler.process(events) if @events_sequence_handler
            rescue Exception => e
              @exceptions_handler.process(Event.new(self, [e, events])) if @exceptions_handler
            end
          end
        end


        def stop(join_thread = false)
          @is_running = false;

          # In test mode Thread.join may lead to DeadLock
          @processor_thread.join if join_thread
        end
      end
    end
  end
end
