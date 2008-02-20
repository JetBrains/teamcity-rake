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
# @date: 05.01.2008 

# For autocompletion
require "rake"
########################################
require "rake/packagetask"

  CLEAN_FILES = FileList['dist']
  CLEAN_FILES.clear_exclude
  task :clean do
    #rm_r CLEAN_FILES, {:verbose => true, :force => true}
    puts ".....Cleaninig....."
  end

  task :copy_ab => :clean do
    puts "Copied.. a->b"
  end


  task :copy_ac => :clean do
    puts "Copied.. a->c"
  end

  task :copy_ac_ab => [:clean, :copy_ab, :copy_ac] do
    puts "Copied.. a->c"
  end