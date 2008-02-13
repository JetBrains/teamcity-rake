# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @data: 13.02.2008

require "spec"

describe "PendingTests" do
  it 'should be IGNORED in teamcity - without block'

  it 'should be IGNORED in teamcity - raise an ExamplePendingError if no block is supplied' do
    pending "TODO"
  end

  it 'should be IGNORED in teamcity - raise an ExamplePendingError if a supplied block fails as expected' do
    pending "TODO (Exception)" do
      raise "Medved Exception"
    end
  end

  it 'should be IGNORED in teamcity - raise a PendingExampleFixedError if a supplied block starts working' do
    pending "TODO" do
      # Do nothing
    end
  end
end