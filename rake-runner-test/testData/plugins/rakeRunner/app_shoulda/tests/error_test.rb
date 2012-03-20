require File.join(File.dirname(__FILE__), 'test_helper.rb')

class ErrorTest < Test::Unit::TestCase
  context "This context" do
    should "contain failed test" do
      2 / 0
      assert_equal 1,1
    end
  end
end