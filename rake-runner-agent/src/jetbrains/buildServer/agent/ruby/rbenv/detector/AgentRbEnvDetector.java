/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class AgentRbEnvDetector {

  @NotNull
  private final RbEnvDetector myDetector;

  private static AgentRbEnvDetector ourInstance;

  public AgentRbEnvDetector(@NotNull final RbEnvDetector detector,
                            @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher) {
    this.myDetector = detector;
    dispatcher.addListener(new Listener());
    ourInstance = this;
  }

  public static AgentRbEnvDetector getInstance() {
    return ourInstance;
  }

  private class Listener extends AgentLifeCycleAdapter {
    @Override
    public void afterAgentConfigurationLoaded(@NotNull BuildAgent agent) {
      final BuildAgentConfiguration buildAgentConfiguration = agent.getConfiguration();
      updateConfig(buildAgentConfiguration);
    }

    @Override
    public void buildFinished(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
      final BuildAgentConfiguration buildAgentConfiguration = build.getAgentConfiguration();
      updateConfig(buildAgentConfiguration);
    }
  }

  private void updateConfig(final BuildAgentConfiguration buildAgentConfiguration) {
    @Nullable InstalledRbEnv installedRbEnv = myDetector.detect(buildAgentConfiguration.getBuildParameters().getEnvironmentVariables());
    myDetector.patchBuildAgentConfiguration(buildAgentConfiguration, installedRbEnv);
  }


}
