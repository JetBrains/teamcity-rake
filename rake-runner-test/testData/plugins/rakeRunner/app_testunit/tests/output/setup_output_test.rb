# @author: Roman Chernyatchik
require "test/unit"

class SetupOutputTest < Test::Unit::TestCase

  # Called before every test method runs. Can be used
  # to set up fixture information.
  def setup
    $stdout << "\nsetup:$stdout<<msg1"
    STDOUT << "\nsetup:STDOUT<<msg2"
    $stderr << "setup:$stderr<<msg3\n"
    STDERR << "setup:STDERR<<msg4\n"
  end

  def test_fake
    assert true
  end
end