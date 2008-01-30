# Copyright 2000-2008 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @date: 05.01.2008

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