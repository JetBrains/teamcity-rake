require 'rubygems'
gem 'test-spec'
require 'test/spec'

context "This context" do
  specify "contain failed test" do
    1.should == 2
  end
end