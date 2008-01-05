# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 05.01.2008
# Time: 20:56:04
# To change this template use File | Settings | File Templates.
require 'test/unit'

class T1Test < Test::Unit::TestCase
  def test_truth
    assert_equal 1, 1
  end
  def test_failing
    assert_equal 1, 2
  end

  def test_compile_error
    assert_equal 1, k
  end
end