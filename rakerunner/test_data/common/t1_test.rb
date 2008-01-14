# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 05.01.2008
# Time: 20:56:04
# To change this template use File | Settings | File Templates.
require 'test/unit'

class T1Test < Test::Unit::TestCase
  def test_true
    assert_equal 1, 1
  end

  def test_failing_large_stack_trace
    my_failure_iterate(10)
  end

  def test_failing_short_stack_trace
    assert_equal 1, 2
  end

  def test_compile_error
    assert_equal 1, k
  end

  def test_5_millisec
    sleep(5) 
    true
  end

  private
  def my_failure_iterate(count)
    p "my_failure_iterate #{count}"
    if (count <= 0)
      my_failure
    else
      my_failure_iterate(count - 1)
    end
  end
  def my_failure
    assert_equal 1, 2
  end
end