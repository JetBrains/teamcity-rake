# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @data: 13.02.2008

require "spec"

describe "PendingTests" do
  it "should be IGNORED in teamcity - without block"

  it "should be IGNORED in teamcity - ExamplePendingError exception" do

    raise ExamplePendingError.new("ExamplePendingError was raised")
  end
end