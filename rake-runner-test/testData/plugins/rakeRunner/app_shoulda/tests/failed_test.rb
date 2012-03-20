require File.join(File.dirname(__FILE__), 'test_helper.rb')

class FailedTest < Test::Unit::TestCase
  context "This context" do
    should "contain failed test" do
      assert_equal 1,2
    end
  end
end