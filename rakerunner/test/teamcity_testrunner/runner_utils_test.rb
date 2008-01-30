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
# @date: 11.01.2008

require File.dirname(__FILE__) + '/../test_helper'

require 'src/test/unit/ui/teamcity/runner_utils'

class RunnerUtilsTest < Test::Unit::TestCase
  include Rake::TeamCity::RunnerUtils

  def test_convert_ruby_test_name
    assert_equal(nil, convert_ruby_test_name(nil))
    assert_equal("", convert_ruby_test_name(""))

    assert_equal("ParamsOfCallSeqTest.test_method_params_syntax_is_ok",
                  convert_ruby_test_name("test_method_params_syntax_is_ok(ParamsOfCallSeqTest)"))
     assert_equal("P.test_method_params_syntax_is_ok",
                  convert_ruby_test_name("test_method_params_syntax_is_ok(P)"))
     assert_equal("test_method_params_syntax_is_ok",
                  convert_ruby_test_name("test_method_params_syntax_is_ok()"))
     assert_equal("test_method_params_syntax_is_ok",
                  convert_ruby_test_name("test_method_params_syntax_is_ok"))

  end

  def test_convert_ruby_test_name_qualified_name
    assert_equal("A.Test.test_1",
                 convert_ruby_test_name("test_1(A::Test)"))
    assert_equal("A::B::C.Test.test_1",
                 convert_ruby_test_name("test_1(A::B::C::Test)"))
    assert_equal("A::B.C.Test.test_1",
                 convert_ruby_test_name("test_1(A::B::C.Test)"))
    assert_equal("A::B.C.Test.test_1",
                 convert_ruby_test_name("test_1(A::B.C::Test)"))
  end
end