require 'test/unit'
require 'rubygems'
gem 'Shoulda'
require 'shoulda'

class FailedTest < Test::Unit::TestCase
  context "This context" do
    should "contain failed test" do
      assert_equal 1,2
    end
  end
end