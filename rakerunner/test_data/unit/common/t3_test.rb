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

  eval("def test_fail_always_new_#{Time.now.to_i}
           assert_equal 2, 3
end")
end