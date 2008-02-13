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

  it 'should raise an ExamplePendingError if no block is supplied' do
    lambda {
      include Pending
      pending "TODO"
    }.should raise_error(ExamplePendingError, /TODO/)
  end

  it 'should raise an ExamplePendingError if a supplied block fails as expected' do
    lambda {
      include Pending
      pending "TODO" do
        raise "oops"
      end
    }.should raise_error(ExamplePendingError, /TODO/)
  end

  it 'should raise a PendingExampleFixedError if a supplied block starts working' do
    lambda {
      include Pending
      pending "TODO" do
        # success!
      end
    }.should raise_error(PendingExampleFixedError, /TODO/)
  end
end