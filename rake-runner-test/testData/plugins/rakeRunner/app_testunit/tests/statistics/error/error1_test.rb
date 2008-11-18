# @author: Roman Chernyatchik
require "test/unit"

class Failed1Test < Test::Unit::TestCase

  def test_error11
    2 / 0
  end

  def test_error12
    require nil
  end
end