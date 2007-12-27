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

#TODO If we runner isn't connected to server, use default Console runner
require 'test/unit/autorunner_old.rb'
require File.expand_path('test/unit/teamcity/event_queue/event_queue')

module Test
  module Unit
    class AutoRunner
      RUNNERS[:teamcity] = proc do |r|
        require File.expand_path(File.dirname(__FILE__) + '/teamcity/testrunner')
        Test::Unit::UI::TeamCity::TestRunner
      end

      alias old_options options
      def options

        puts "!!!!!!!!!!!!!11 Set? #{Rake::TeamCity::MessagesDispather.teamcity_test_runner_enabled_set?}"
        # use teamcity runner by default
        if Rake::TeamCity::MessagesDispather.teamcity_test_runner_enabled_set?
          @runner = RUNNERS[:teamcity]
        end
        # options can override this default params
        old_options
      end

#  TODO
       def initialize(standalone)
         puts "!!!!!!!!!!!!!11 Set? #{Rake::TeamCity::MessagesDispather.teamcity_test_runner_enabled_set?}"
         super

#         @runner = RUNNERS[:teamcity]
       end
    end
  end
end


module Rake
  module TeamCity
    require 'test/unit/assertions'

    include Test::Unit::Assertions

    def run_tests(file_pattern='test/test*.rb',
                  log_enabled = false,
                  additional_options = nil)

      Dir["#{file_pattern}"].each { |fn|
        puts fn if log_enabled
        begin
          Test::Unit::AutoRunner.run(true, nil, additional_options ? [fn] + additional_options : [fn])
        rescue RuntimeError => ex
          # TeamCity test runner will process this exception
          # TODO Terminate?
        end
      }
    end

    extend self
  end
end