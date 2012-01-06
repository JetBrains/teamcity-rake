# @author: Roman Chernyatchik
require "rspec"

describe "Compilation" do

  # Called after each example.
  after(:each) do
    $
  end

  it "should fail in after" do
    true.should == true
  end
end