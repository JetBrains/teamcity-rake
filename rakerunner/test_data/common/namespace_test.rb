# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 05.01.2008
# Time: 20:56:04
# To change this template use File | Settings | File Templates.
require 'test/unit'

module A
  module B
    module C

    end
  end
end

class A::B::C::Namespace_Test < Test::Unit::TestCase
  def test_true
    assert_equal 2, 2
  end

  def test_failure
    assert_equal 2, 3
  end
end