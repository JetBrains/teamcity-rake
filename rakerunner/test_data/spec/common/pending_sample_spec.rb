# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @data: 13.02.2008

require "spec"

describe "PendingTests" do
  it "should be IGNORED in teamcity - without block"

  it "should be IGNORED in teamcity - ExamplePendingError exception with msg" do

    raise Spec::Example::ExamplePendingError, "ExamplePendingError was raised", caller
    end

  it "should be IGNORED in teamcity - ExamplePendingError exception without msg" do

    raise Spec::Example::ExamplePendingError, caller
  end

  it "should be IGNORED in teamcity - ExamplePendingError exception without msg and stacktrace" do

    raise Spec::Example::ExamplePendingError
  end
end