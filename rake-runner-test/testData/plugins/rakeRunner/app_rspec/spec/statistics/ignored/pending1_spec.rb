# @author: Roman Chernyatchik
require "rspec"

describe "Pending" do

  it "should pending method" do
    pending("get sing of medved")
    medved.should say("preved")
  end

  it "should pending block" do
    pending("get sing of krevedko") do
      krevedko.should be("ya!")
    end
  end

  it "should be fixed pending" do
    pending("some") do
      true.should == true
    end
  end
end