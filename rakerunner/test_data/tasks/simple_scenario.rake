# For autocompletion
require "rake"
########################################
namespace :simple_sc do
  task :dist => [:create_fake_dirs, :clean] do
  end

  task :create_fake_dirs do
    puts "Current dir: #{File.expand_path(".")}"
    user_block("Fake progress") do
      30.times do |i|
        dir_name = "dist/dir#{i}"
        mkdir_p dir_name
        cp_r "common", dir_name
      end
      user_msg "Fake status message" #File.expand_path(".")
    end
  end

  CLEAN_FILES = FileList['dist/**/*']
  CLEAN_FILES.clear_exclude
  task :clean do
    rm(CLEAN_FILES, :verbose)
  end
end
#############################################
def user_block(name)
  puts "##[#{name}"
  yield
  puts "##]#{name}"
end

def user_msg(text)
  puts "###{text}"
end