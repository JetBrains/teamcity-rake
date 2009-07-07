require 'rubygems'
gem 'test-spec'
require 'test/spec'

context "This context" do
  specify "contain passing test 3" do
    1.should == 1
  end
end