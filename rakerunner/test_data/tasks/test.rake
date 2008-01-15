# @author: Roman.Chernyatchik
# @date: 27.12.2007

# Ruby Tests, for testing buildserver view
Rake::TestTask.new(:test_data_common) do |t|
  #t.libs << ".."
  t.test_files = FileList['common/**/*_test.rb']
end
desc "Common tests via Rake.run_tests"
task :test_data_common1 do
  require "rake/runtest"
  Rake.run_tests 'common/**/*_test.rb'
end


# Test with compilation errors.
Rake::TestTask.new(:test_data_compile_failure) do |t|
  t.test_files = FileList['compilation_errors/**/*_test.rb']
end
desc "Common tests via Rake.run_tests"
task :test_data_compile_failure1 do
end

