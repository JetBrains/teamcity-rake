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
# @date: 11.02.2008

require 'rake'
require 'spec/rake/spectask'

desc "Run all examples"
Spec::Rake::SpecTask.new('spec_examples') do |t|
  t.spec_files = FileList['spec/common/**/*_spec.rb']
  t.spec_opts = ["--require spec/runner/formatter/teamcity/formatter",
                 "--format Spec::Runner::Formatter::TeamcityFormatter:matrix"]
  t.warning = true
  # t.fail_on_error = false;
  # t.rcov = true #TODO
end

Spec::Rake::SpecTask.new('spec_examples_compilation_failure') do |t|
  t.spec_files = FileList['spec/compilation_errors/**/*_spec.rb']
  t.spec_opts = ["--require", "spec/runner/formatter/teamcity/formatter",
                 "--format", "Spec::Runner::Formatter::TeamcityFormatter:matrix"]
  t.fail_on_error = false;
end