require "rake"
require "rake/testtask"

########################################
namespace :stat do

  Rake::TestTask.new(:general) do |t|
    t.test_files = FileList['tests/statistics/general/**/*_test.rb']
  end

  Rake::TestTask.new(:passed) do |t|
    t.test_files = FileList['tests/statistics/passed/**/*_test.rb']
  end

  Rake::TestTask.new(:failed) do |t|
    t.test_files = FileList['tests/statistics/failed/**/*_test.rb']
  end

  Rake::TestTask.new(:error) do |t|
    t.test_files = FileList['tests/statistics/error/**/*_test.rb']
  end

  Rake::TestTask.new(:compile_error) do |t|
    t.test_files = FileList['tests/statistics/compile_error/**/*_test.rb']
  end
end