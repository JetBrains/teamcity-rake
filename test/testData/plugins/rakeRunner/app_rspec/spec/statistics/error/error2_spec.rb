# @author: Roman Chernyatchik
require "spec"

describe "Spec error" do

  it "should error2" do
    2/0
    true.should == true
  end

end