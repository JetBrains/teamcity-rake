require 'test/unit'
require 'rubygems'
gem 'Shoulda'
require 'shoulda'

class Pass1Test < Test::Unit::TestCase
  context "This context" do
    should "contain passing test 1" do
      assert_equal 1,1
    end
    should "contain passing test 2" do
      assert_equal 1,1
    end
  end

  context "Other context" do
    should "also contains passing test" do
      assert_equal 1,1
    end
  end
end