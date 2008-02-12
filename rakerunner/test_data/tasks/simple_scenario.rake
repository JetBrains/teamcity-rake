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

namespace :simple_sc do
  CLEAN_FILES = FileList['dist']
  CLEAN_FILES.clear_exclude
  task :clean do
    rm_r CLEAN_FILES, {:verbose => true, :force => true}
  end

  task :create_zip do
    puts "Current dir: #{File.expand_path(".")}"
    user_block("Fake progress") do
      200.times do |i|
        dir_name = "dist/dir#{i}"
        user_msg "Fake status message : #{dir_name}"
        mkdir_p dir_name
        list = FileList['unit/common/**/*']
        list.exclude("**/*/.svn")
        list.each do |file|
          cp_r file, dir_name + "/common", {:verbose => true}
        end
      end
    end
  end

  task :remove_tmp_files do
    rm_r FileList['dist/dir*']
  end

  REVISION =  ENV["BUILD_VCS_NUMBER.1"] ? ENV["BUILD_VCS_NUMBER.1"] : "<revision>" 
  Rake::PackageTask.new("sample", "0.1." + REVISION) do |p|
    p.need_zip = true
    p.zip_command = "7z a -mx=9 -mmt=off"
    p.package_dir = "dist"
    p.package_files.include("dist/dir**/*")
  end

  task :build_zip => [:create_zip, :package, :remove_tmp_files]

  task :dist => [:clean, :build_zip] do
  end
end

#############################################
def user_block(name)
  puts "##[#{name}"
  begin
    yield
  ensure
    puts "##]#{name}"
  end  
end

def user_msg(text)
  puts "###{text}"
end