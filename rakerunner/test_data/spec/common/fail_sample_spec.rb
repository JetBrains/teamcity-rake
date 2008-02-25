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
# @date: 12.02.2008

require "spec"

describe "Simple test" do
  it "should fails" do
    true.should == true
  end
end

describe "Test Data" do
  describe "(fail Behaviour)" do
    it "should fail - should be - equal" do
      2.should == 3
    end

    it "should fail - should be - empty" do
      [1].should be_empty
    end

    it "should fail - should raise error (other)" do
      lambda {2 + nil}.should raise_error(StackOverflowError)
    end
    it "should fail - should raise error (no error)" do
      lambda {2 + 3}.should raise_error(StackOverflowError)
    end
  end
end