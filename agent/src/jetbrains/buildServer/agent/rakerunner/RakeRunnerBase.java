/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.rakerunner;

import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.agent.runner.GenericProgramRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 01.06.2007
 */
public abstract class RakeRunnerBase extends GenericProgramRunner implements RakeRunnerConstants {

    public boolean canRun(final BuildAgentConfiguration agentConfiguration) {
        return true;
    }

    @SuppressWarnings({"NoopMethodInAbstractClass"})
    protected void processTerminated(final RunEnvironment runEnv, final boolean isFailed) {
        super.processTerminated(runEnv, isFailed);
        //TODO
    }

    protected void fillSystemProperties(final Map<String, String> props,
                                        final Map<String, String> runParameters,
                                        final Map<String, String> buildParameters) {
        props.putAll(extractParametersFrom(buildParameters, Constants.SYSTEM_PREFIX));
        for (String property : AgentRuntimeProperties.PROPERTIES) {
            final String value = runParameters.get(property);
            if (value != null) {
                props.put(property, value);
            }
        }
    }

    protected String getRunnerNotRegisteredMessage() {
      return "Runner " + getType() + " is not registered: ";
    }
}
