# @author: Roman Chernyatchik
require "spec"

describe "Compilation" do

  # Called before each example.
  before(:each) do
    $
  end

  # Called after each example.
  after(:each) do
    # Do nothing
  end

  it "should fail in before" do
    true.should == true
  end
end