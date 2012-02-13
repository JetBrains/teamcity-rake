# Copyright 2000-2012 JetBrains s.r.o.
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
# @date: 05.01.2008 

# For autocompletion
require "rake"

########################################
namespace :build_script do
  task :std_out do
    puts "puts.msg1"
    $stdout << "$stdout<<msg2\n"
    STDOUT << "STDOUT<<msg3\n"

    $stdout.flush
    STDOUT.flush
  end

  task :std_out2 do
    $stdout << "$stdout<<msg1"

    STDOUT << "\nSTDOUT<<msg2\n"
    $stdout.flush
    STDOUT.flush
  end

  task :std_out3 do
    $stdout << "$stdout<<msg1\n"
    STDOUT << "STDOUT<<msg2\n"

    $stdout.flush
    STDOUT.flush
  end

  task :std_err do
    $stderr << "$stderr<<msg1\n"
    STDERR << "STDERR<<msg2\n"

    $stderr.flush
    STDERR.flush
  end

  task :std_err2 do
    $stderr << "$stderr<<msg1\n"
    STDERR << "STDERR<<msg2\n"

    $stderr.flush
    STDERR.flush
  end

  task :std_out_external do
    ruby "-e", %{$stdout << '$stdout_<<_external\n'}
  end
  task :std_err_external do
    ruby "-e", %{$stderr << '$stderr_<<_external\n'}
  end

  task :std_out_err_wo_newline do
    $stdout << "$stdout"
    STDOUT << "STDOUT"
    $stderr << "$stderr"
    STDERR << "STDERR"

    $stdout.flush
    STDOUT.flush
    $stderr.flush
    STDERR.flush
  end

  task :show_one_task do
  end

  task :exception_in_task do
    2 / 0
  end

  task :exception_in_embedded_task => :exception_in_task do
  end

  task :warning_in_task do
    MY_CONST = 5
    MY_CONST = 6
  end

  task :my_default_task do    
  end

  task :some_task0 do

  end
  task :some_task1 do

  end

  task :first_time_check do
    Rake::Task["build_script:some_task0"].invoke
    Rake::Task["build_script:some_task0"].invoke
  end

  task :embedded_tasks => [:some_task0, :some_task1] do
  end

  task :cmd_failed do
    ruby "-e", "2/0"
  end

  task  :depends_on_cmd_failed => :cmd_failed do
    puts "finished task"
  end

  task :task_args do |t, args|
    project_list = ["rake", "msbuild"]
    # rake shold be >= 0.8.7
    args.with_defaults(:projects => project_list)
    args.projects.each do |p|
      puts "Project: #{p}"
    end
  end
end