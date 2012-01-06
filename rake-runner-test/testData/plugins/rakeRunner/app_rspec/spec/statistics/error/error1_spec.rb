# @author: Roman Chernyatchik
require "rspec"

describe "Spec error" do

  it "should error11" do
    2/0
    true.should == true
  end

  it "should error12" do
    2/0
    true.should == true
  end
end