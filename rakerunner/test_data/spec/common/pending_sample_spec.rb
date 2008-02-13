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



module Spec
  module DSL
    describe Pending do
      it 'should raise an ExamplePendingError if no block is supplied' do
        include Pending
        pending "TODO"
      end

      it 'should raise an ExamplePendingError if a supplied block fails as expected' do
        include Pending
        pending "TODO" do
          raise "oops"
        end
      end

      it 'should raise a PendingExampleFixedError if a supplied block starts working' do
        include Pending
        pending "TODO" do
          # success!
        end
      end
    end
  end
end