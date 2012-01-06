require "rake"
require 'rspec/core/rake_task'

########################################
namespace :output do

  RSpec::Core::RakeTask.new(:spec_output) do |t|
    t.pattern = 'spec/output/**/*_spec.rb'
  end
end