# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 05.01.2008
# Time: 20:56:04
# To change this template use File | Settings | File Templates.
require 'test/unit'

class T3_WITH_OUTPUT_Test < Test::Unit::TestCase
  def test_true
    assert_equal 2, 2
  end

  def test_true_err_out_puts
    $stderr << "test_true.$stderr"
    $stdout << "test_true.$stdout"
    puts("test_true.puts")

    assert_equal 2, 2
  end

  def test_failing_err_out_puts
    $stderr << "test_true.$stderr"
    $stdout << "test_true.$stdout"
    puts("test_true.puts")

    assert_equal 2, 4
  end

  def test_true_err_out_puts_child_process
    system("ls medved_krevedko_and_preved_are_our_best_friends_")
    system("dir medved_krevedko_and_preved_are_our_best_friends_")

    assert_equal 2, 2
  end

  def test_false_err_out_puts_child_process
    system("ls medved_krevedko_and_preved_are_our_best_friends_")
    system("dir medved_krevedko_and_preved_are_our_best_friends_")

    assert_equal 2, 3
  end

end