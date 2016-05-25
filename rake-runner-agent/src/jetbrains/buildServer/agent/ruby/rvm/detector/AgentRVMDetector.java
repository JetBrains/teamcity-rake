/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby.rvm.detector;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class AgentRVMDetector {

  @NotNull
  private final RVMDetector myDetector;

  private static AgentRVMDetector ourInstance;

  public AgentRVMDetector(@NotNull final RVMDetector detector,
                          @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher) {
    this.myDetector = detector;
    dispatcher.addListener(new Listener());
    ourInstance = this;
  }

  public static AgentRVMDetector getInstance() {
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
    @Nullable InstalledRVM rvm = myDetector.detect(buildAgentConfiguration.getBuildParameters().getEnvironmentVariables());
    myDetector.patchBuildAgentConfiguration(buildAgentConfiguration, rvm);
  }


}
