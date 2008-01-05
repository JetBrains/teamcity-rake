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
# @date: 08.06.2007

require File.dirname(__FILE__) + '/../test_helper'

require 'src/rake_ext'

class Object
  @@test_stdout = ""
  @@test_stderr = ""

  def self.test_stdout=(val)
    @@test_stdout = val
  end

  def self.test_stdout
    @@test_stdout
  end

  def self.test_stderr=(val)
    @@test_stderr = val
  end

  def self.test_stderr
    @@test_stderr
  end

  def puts(obj)
    @@test_stdout << ("[IDEA]: " + obj.to_s)
  end

  def log_error(obj)
    @@test_stderr << ("[IDEA]: " + obj.to_s)
  end
end

module XMLRPC

  class Client
     attr_reader :host
     attr_reader :port
     attr_reader :path
  end
end

module Rake
  class TeamCityApplication < Application
    @@test_warning = ''
    
    def self.test_warning
       @@test_warning
     end

    def self.test_warning=(val)
       @@test_warning = val
     end

    def self.send_warning(msg)
       @@test_warning << ("[IDEA]: " + msg.to_s) 
    end
  end
end
#######################################################################
#######################################################################
###################    Tests     ######################################
#######################################################################
#######################################################################


class ModuleTest < Test::Unit::TestCase

  def test_rake_extension
    Object.test_stdout= ""
    Object.test_stderr= ""

    String.rake_extension("to_s") do
      Object.test_stderr = "ok"
    end

    assert_equal("[IDEA]: WARNING: Possible conflict with Rake extension: String#to_s already exists",
                 Rake::TeamCityApplication.test_warning)
    assert_equal("", Object.test_stdout)
    assert_equal("", Object.test_stderr)

    Rake::TeamCityApplication.test_warning = ""
    String.rake_extension("ya_krevetko") do
      Object.test_stderr = "ok"
    end
    assert_equal("",
                 Rake::TeamCityApplication.test_warning)
    assert_equal("ok", Object.test_stderr)
    assert_equal("", Object.test_stdout)
  end
end

#######################################################################
#######################################################################

class RakeTest < Test::Unit::TestCase
  def test_application
    app = Rake.application

    assert_not_nil app
    assert_same app, Rake.application
    assert_equal app.class, Rake::TeamCityApplication
  end

end

#######################################################################
#######################################################################
