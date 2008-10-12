require "rake"
require "rake/testtask"

########################################
namespace :stat do

  Rake::TestTask.new(:general) do |t|
    t.test_files = FileList['tests/statistics/general/**/*_test.rb']
  end
end