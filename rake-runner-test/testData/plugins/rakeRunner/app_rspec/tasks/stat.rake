require "rake"
require 'rspec/core/rake_task'

########################################
namespace :stat do

  RSpec::Core::RakeTask.new(:general) do |t|
    t.pattern = 'spec/statistics/general/**/*_spec.rb'
  end

  RSpec::Core::RakeTask.new(:passed) do |t|
    t.pattern = 'spec/statistics/passed/**/*_spec.rb'
  end

  RSpec::Core::RakeTask.new(:failed) do |t|
    t.pattern = 'spec/statistics/failed/**/*_spec.rb'
  end

  RSpec::Core::RakeTask.new(:error) do |t|
    t.pattern = 'spec/statistics/error/**/*_spec.rb'
  end

  RSpec::Core::RakeTask.new(:ignored) do |t|
    t.pattern = 'spec/statistics/ignored/**/*_spec.rb'
  end

  RSpec::Core::RakeTask.new(:compile_error) do |t|
    t.pattern = 'spec/statistics/compile_error/**/*_spec.rb'
  end
end