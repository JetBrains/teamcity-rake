# Copyright 2000-2008 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @date: 13.02.2008

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

  it 'should be IGNORED in teamcity - raise a PendingFixedError if a supplied block starts working' do
    pending "TODO" do
      # Do nothing
    end
  end
end