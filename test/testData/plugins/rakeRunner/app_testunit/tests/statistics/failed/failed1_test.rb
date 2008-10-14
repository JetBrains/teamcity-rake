# @author: Roman Chernyatchik
require "test/unit"

class Failed1Test < Test::Unit::TestCase

  def test_failed11
    assert false
  end

  def test_failed12
    fail
  end

  def test_pass13
    fail("failure message")
  end
end