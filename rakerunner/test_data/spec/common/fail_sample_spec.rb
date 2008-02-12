# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @data: 12.02.2008

require "spec"

describe "Simple test" do
  it "should fails" do
    true.should == true
  end
end

describe "Test Data" do
  describe "(fail Behaviour)" do
    it "should fail - should be - equal" do
      2.should == 3
    end

    it "should fail - should be - empty" do
      [1].should be_empty
    end

    it "should fail - should raise error (other)" do
      lambda {2 + nil}.should raise_error(StachOverflowError)
    end
    it "should fail - should raise error (no error)" do
      lambda {2 + 3}.should raise_error(StachOverflowError)
    end
  end
end