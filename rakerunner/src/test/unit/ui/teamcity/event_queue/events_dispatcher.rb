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

require 'thread'
require 'test/unit/ui/teamcity/event_queue/events_processor'

#module Rake::TeamCity::Logger
module Rake::TeamCity::Logger
  class EventsDispatcher
    def initialize(events_sequence_handler, exceptions_handler = nil)
      @queue = Queue.new
      @processor = EventsProcessor.new(@queue, events_sequence_handler, exceptions_handler)
    end

    # Adds new event into the event queue
    def dispatch event
      @queue.push event
    end

    def start
      @processor.start
    end

    # Stops events processor and wait untill all events will be processed
    def stop(join_thread = false)
      @processor.stop(join_thread)
    end

    # Stops events processor and doesn't process all remaing events
    def stop_immediately
      @processor.stop_immediately
    end
  end
end
