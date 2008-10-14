require "rake"
require "rake/testtask"

########################################
namespace :tests do

  Rake::TestTask.new(:test_output) do |t|
    t.test_files = FileList['tests/output/**/*_test.rb']
  end
end