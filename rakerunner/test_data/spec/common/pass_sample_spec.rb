# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @data: 12.02.2008

require "spec"

describe "Simple test" do
  it "should pass" do
    true.should == true
  end
end

describe "Test Data" do
  describe "(pass Behaviour)" do
    it "should pass - should be - equal" do
      2.should == 2
    end

    it "should pass - should be - empty" do
      [].should be_empty
    end

    it "should pass - should raise error" do
      lambda {2 + nil}.should raise_error(StachOverflowError)
    end
  end
end