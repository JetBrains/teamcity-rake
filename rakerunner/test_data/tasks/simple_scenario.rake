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
        list = FileList['common/**/*']
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

  Rake::PackageTask.new("sample", "0.1") do |p|
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