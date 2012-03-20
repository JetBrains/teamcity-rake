require 'rubygems'

if (RUBY_VERSION.to_f == 1.9) then
#  gem 'minitest'
#  gem 'minitest-reporters'
#
#  p $:
#
#  require 'minitest/reporters'
#
#  m = MiniTest::Unit.method(:autorun)
#  p m
#  p m.source_location
#
#  MiniTest::Unit.runner = MiniTest::SuiteRunner.new
#  if ENV["RM_INFO"] || ENV["TEAMCITY_VERSION"]
#    MiniTest::Unit.runner.reporters << MiniTest::Reporters::RubyMineReporter.new
#  end
#  require 'shoulda'
  require 'test/unit'
  require 'rubygems'
  gem 'shoulda'
  require 'shoulda'
else
  require 'test/unit'
  require 'rubygems'
  gem 'shoulda'
  require 'shoulda'
end


