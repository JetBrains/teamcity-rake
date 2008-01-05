# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 05.01.2008
# Time: 20:56:04
# To change this template use File | Settings | File Templates.
require 'test/unit'

class T2Test < Test::Unit::TestCase
  def test_true
    assert_equal 2, 3
  end
  def test_failing
    assert_equal 2, 4
  end
end