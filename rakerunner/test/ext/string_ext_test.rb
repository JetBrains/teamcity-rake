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
# @date: 04.05.2007

require 'test/unit'
require 'test/unit/ui/teamcity/message_factory'

class String; def rescue_action(e) raise e end; end

class StringTest < Test::Unit::TestCase

  def test_starts_with?
    assert("teamcity".starts_with?("team"))
    assert("teamcity".starts_with?("teamcity"))
    assert("teamcity".starts_with? "t")
    assert("teamcity".starts_with? (""))

    assert !("teamcity".starts_with?("e"))
    assert !("teamcity".starts_with?("teamcity!"))
  end

  def test_substring
    assert_equal("teamcity", "teamcity".substring(0))
    assert_equal("eamcity", "teamcity".substring(1))
    assert_equal("ity", "teamcity".substring(5))
    assert_equal("y", "teamcity".substring(7))  
    assert_equal("", "teamcity".substring(8))
    assert_equal(nil, "teamcity".substring(9))
  end
end
