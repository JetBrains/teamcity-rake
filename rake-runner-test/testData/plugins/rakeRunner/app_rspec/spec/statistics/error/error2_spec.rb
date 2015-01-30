# @author: Roman Chernyatchik
require "rspec"

describe "Spec error" do

  it "should error2" do
    2/0
    expect(true).to eq(true)
  end

end