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
# @date: 27.12.2007

# Ruby Tests, for testing buildserver view
Rake::TestTask.new(:test_data_common) do |t|
  #t.libs << ".."
  t.test_files = FileList['common/**/*_test.rb']
end
desc "Common tests via Rake.run_tests"
task :test_data_common1 do
  require "rake/runtest"
  Rake.run_tests 'common/**/*_test.rb'
end


# Test with compilation errors.
Rake::TestTask.new(:test_data_compile_failure) do |t|
  t.test_files = FileList['compilation_errors/**/*_test.rb']
end
desc "Common tests via Rake.run_tests"
task :test_data_compile_failure1 do
end

