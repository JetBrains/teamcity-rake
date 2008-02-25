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

module Rake
  module TeamCity
    module RunnerUtils

      # Converts Ruby Test Names : $TEST_METHOD_NAME($TEST_CASE_QUALIFIED_NAME)
      # to TeamCity format :  $NAMESPACE.$TEST_CASE_NAME.$TEST_METHOD_NAME
      def convert_ruby_test_name(ruby_name)
        if ruby_name && (ruby_name.strip =~ /(\w+)\(([\w:]*)\)/)
          # p [$1, $2]
          method_name = $1
          qualified_name = $2
          return (
                  if qualified_name.empty?
                    "#{method_name}"
                  else
                    "#{qualified_name}.#{method_name}"
                  end)
        end
        ruby_name
      end
    end
  end
end