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
if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/rakerunner_consts'
else
  require 'test/unit/ui/teamcity/rakerunner_consts'
end

#TODO remove ".rb" from end
ORIGINAL_SDK_AUTORUNNER_PATH = ENV[ORIGINAL_SDK_AUTORUNNER_PATH_KEY]
if ORIGINAL_SDK_AUTORUNNER_PATH
  require ORIGINAL_SDK_AUTORUNNER_PATH
end

if ENV["idea.rake.debug.sources"]
  require 'src/test/unit/ui/teamcity/event_queue/messages_dispatcher'
else
  require 'test/unit/ui/teamcity/event_queue/messages_dispatcher'
end

module Test
  module Unit
    class AutoRunner
      RUNNERS[:teamcity] = proc do |r|
        if ENV["idea.rake.debug.sources"]
          require 'src/test/unit/ui/teamcity/testrunner'
        else
          require 'test/unit/ui/teamcity/testrunner'
        end
        Test::Unit::UI::TeamCity::TestRunner
      end

       alias old_initialize initialize
       private :old_initialize
       def initialize(*args)
         method(:old_initialize).arity == 0 ? old_initialize() : old_initialize(args)
         old_initialize(*args)

         if (Rake::TeamCity::MessagesDispather.teamcity_test_runner_enabled_set?)
           @runner = RUNNERS[:teamcity]
         end
       end
    end
  end
end
#
#module Rake
#  module TeamCity
#    require 'test/unit/assertions'
#
#    include Test::Unit::Assertions
#
#    def run_tests(file_pattern='test/test*.rb',
#                  log_enabled = false,
#                  additional_options = nil)
#
#      Dir["#{file_pattern}"].each { |fn|
#        puts fn if log_enabled
#        begin
#          Test::Unit::AutoRunner.run(true, nil, additional_options ? [fn] + additional_options : [fn])
#        rescue RuntimeError => ex
#          # TeamCity test runner will process this exception
#          # TODO Terminate?
#        end
#      }
#    end
#
#    extend self
#  end
#end