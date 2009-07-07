require 'test/unit'
require 'rubygems'
gem 'Shoulda'
require 'shoulda'

class Pass2Test < Test::Unit::TestCase
  context "This context" do
    should "contain passing test 3" do
      assert_equal 1,1
    end
  end
end