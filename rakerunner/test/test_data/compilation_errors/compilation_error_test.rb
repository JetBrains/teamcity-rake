# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 05.01.2008
# Time: 20:56:04
# To change this template use File | Settings | File Templates.
require 'test/unit'

class T4_COMPILATION_PROBLEM_Test < Test::Unit::TestCase
  include GREEN_MONSTERS_WITH_RED_EYES

  def test_true
    assert_equal 2, 2
  end
end