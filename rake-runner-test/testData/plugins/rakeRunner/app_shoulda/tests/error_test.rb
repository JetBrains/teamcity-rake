require 'test/unit'
require 'rubygems'
gem 'shoulda'
require 'shoulda'

class ErrorTest < Test::Unit::TestCase
  context "This context" do
    should "contain failed test" do
      2 / 0
      assert_equal 1,1
    end
  end
end