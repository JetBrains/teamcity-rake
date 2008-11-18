# @author: Roman Chernyatchik
require "spec"

describe "Spec failed" do

  it "should failed11" do
    true.should == false
  end

  it "should failed12" do
    2.should eql 3
  end
end