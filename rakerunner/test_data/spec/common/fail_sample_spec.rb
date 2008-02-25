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

    it "should fails - should raise error: uninitialized constant" do
      lambda {}.should raise_error(Uninitialized_Constant)
    end

    it "should fail - should raise error (no error)" do
      lambda {2 + 3}.should raise_error(StackOverflowError)
    end

    it "should fail - should have stdout output" do
      $stdout << "Some stdout data\n"

      2.should == 3
    end

    it "should fail - should have stderr output" do
      $stderr << "Some stderr data\n"

      2.should == 3
    end

    it "should fail - should have stderr and stdout output" do
      $stdout << "Some stdout data\n"
      $stderr << "Some stderr data\n"

      2.should == 3
    end

    it "should fail - should have stderr from externel process output" do
      system("ls medved_krevedko_and_preved_are_our_best_friends_")
      system("dir medved_krevedko_and_preved_are_our_best_friends_")

      2.should == 3
    end

    eval("it \"should fail and be always new (##{Time.now.to_i})\" do
             2.should == 3
          end")
  end
end