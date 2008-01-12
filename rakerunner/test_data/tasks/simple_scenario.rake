# For autocompletion
require "fileutils"
include FileUtils
########################################
namespace :simple_sc do
  require 'rake/clean'
  CLEAN.include("dist/**/*")

  #task :dist => :dist_dir do
  desc "Main task"
  task :dist do
    puts "Current dir: #{File.expand_path(".")}"

    user_block("Fake progress") do
      30.times do |i|
        mkdir_p "dist/dir#{i}"
      end

      user_msg "Fake status message" #File.expand_path(".")

    end
  end
end

def user_block(name)
  puts "##[#{name}"
  yield
  puts "##]#{name}"
end

def user_msg(text)
  puts "###{text}"
end