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
# @date: 29.01.2008

# Behaviour properties
  # Debug
TEAMCITY_RAKERUNNER_LOG_RSPEC_XML_MSFS_KEY = 'teamcity.rake.runner.debug.log.rspec.xml.msgs'
TEAMCITY_RAKERUNNER_LOG_PATH_KEY = 'teamcity.rake.runner.debug.log.path'
TEAMCITY_RAKERUNNER_LOG_OUTPUT_HACK_DISABLED_KEY = 'teamcity.rake.runner.debug.output.hack.disabled'
TEAMCITY_RAKERUNNER_LOG_OUTPUT_CAPTURER_DISABLED_KEY = 'teamcity.rake.runner.debug.output.capturer.disabled'
  # Log files
TEAMCITY_RAKERUNNER_LOG_FILENAME_SUFFIX = '/rakeRunner_rake.log'
TEAMCITY_RAKERUNNER_RPC_LOG_FILENAME_SUFFIX = '/rakeRunner_xmlrpc.log'
TEAMCITY_RAKERUNNER_SPEC_LOG_FILENAME_SUFFIX = '/rakeRunner_rspec.log'
TEAMCITY_RAKERUNNER_TESTUNIT_LOG_FILENAME_SUFFIX = '/rakeRunner_testUnit.log'

# Teamcity connection properties
IDEA_BUILDSERVER_BUILD_ID_KEY = 'idea.build.server.build.id'
IDEA_BUILDSERVER_AGENT_PORT_KEY = 'idea.build.agent.port'

# Name of Teamcity RPC logger method
TEAMCITY_LOGGER_RPC_NAME = "buildAgent.log"

# Rake runner dispather settings
TEAMCITY_RAKERUNNER_DISPATCHER_MAX_ATTEMPS = 100
TEAMCITY_RAKERUNNER_DISPATCHER_RETRY_DELAY = 0.25

# Rakerunner system properties
ORIGINAL_SDK_AUTORUNNER_PATH_KEY = 'rake.runner.original.sdk.test.unit.autorunner.path'
TEAMCITY_RAKERUNNER_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED = 'teamcity.rake.runner.rake.trace.invoke.exec.stages.enabled'