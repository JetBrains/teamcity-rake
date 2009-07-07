require 'rubygems'
gem 'test-spec'
require 'test/spec'

context "This context" do
  specify "contain error test" do
    2 / 0
    1.should == 1
  end
end