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
# @date: 14:58:02

module Rake
  module SendMessagesUtil
    def send_create_ruby_flow_message()
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_flow_message}
    end

    def send_error(msg, stacktrace)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_error_message(msg, stacktrace)}
    end

    def send_warning(msg)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg, :warning)}
    end

    def send_info_msg(msg)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg)}
    end

    def send_error_msg(msg)
       send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg, :error)}
    end

    def send_open_target(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_open_block(msg, :target)}
    end

    def send_close_target(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_close_block(msg, :target)}
    end

    def send_normal_user_message(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_user_message(msg)}
    end

    def send_captured_stdout(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg)}
    end

    def send_captured_stderr(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg, :error)}
    end

    def send_captured_warning(msg)
      send_xml_to_teamcity {Rake::TeamCity::MessageFactory.create_message(msg, :warning)}
    end

    def send_xml_to_teamcity
      Rake::TeamCity.msg_dispatcher.log_one(yield)
    end
  end
end
