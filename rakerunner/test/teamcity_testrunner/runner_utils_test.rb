# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 11.01.2008
# Time: 15:46:19
# To change this template use File | Settings | File Templates.
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

     assert_equal("A::B::C::Test.test_1",
                  convert_ruby_test_name("test_1(A::B::C::Test)"))
  end
end