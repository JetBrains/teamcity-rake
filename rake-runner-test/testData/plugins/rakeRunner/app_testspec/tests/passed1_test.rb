require 'rubygems'
gem 'test-spec'
require 'test/spec'

context "This context" do
  specify "contain passing test 1" do
    1.should == 1
  end

  specify "contain passing test 2" do
    1.should == 1
  end
end

context "Other context" do
  specify "also contains passing test" do
    1.should == 1
  end
end