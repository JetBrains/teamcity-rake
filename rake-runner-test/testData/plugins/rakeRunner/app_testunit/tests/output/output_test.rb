# @author: Roman Chernyatchik
require "test/unit"

class OutputTest < Test::Unit::TestCase

  def test_pass
    $stdout << "\ntest:$stdout<<msg1"
    STDOUT << "\ntest:STDOUT<<msg2"
    $stderr << "test:$stderr<<msg3\n"
    STDERR << "test:STDERR<<msg4\n"

    $stdout.flush
    STDOUT.flush

    $stderr.flush
    STDERR.flush

    assert true
  end

  def test_fail
    $stdout << "\ntest:$stdout<<msg5"
    STDOUT << "\ntest:STDOUT<<msg6"
    $stderr << "test:$stderr<<msg7\n"
    STDERR << "test:STDERR<<msg8\n"

    $stdout.flush
    STDOUT.flush

    $stderr.flush
    STDERR.flush

    fail
  end

  def test_error
    $stdout << "\ntest:$stdout<<msg9"
    STDOUT << "\ntest:STDOUT<<msg10"
    $stderr << "test:$stderr<<msg11\n"
    STDERR << "test:STDERR<<msg12\n"

    $stdout.flush
    STDOUT.flush

    $stderr.flush
    STDERR.flush

    2 / 0
  end
end