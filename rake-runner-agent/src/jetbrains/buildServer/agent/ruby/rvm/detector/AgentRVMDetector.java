/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.agent.ruby.ConfigurationApplier;
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
                          @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                          @NotNull final BuildAgentConfiguration buildAgentConfiguration,
                          @NotNull final ExtensionHolder extensionHolder) {
    this.myDetector = detector;
    dispatcher.addListener(new Listener());
    extensionHolder.registerExtension(AgentParametersSupplier.class, getClass().getName(), new Snapshot(buildAgentConfiguration));
    ourInstance = this;
  }

  public static AgentRVMDetector getInstance() {
    return ourInstance;
  }

  private class Listener extends AgentLifeCycleAdapter {
    @Override
    public void buildFinished(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
      final BuildAgentConfiguration buildAgentConfiguration = build.getAgentConfiguration();
      @Nullable InstalledRVM rvm = myDetector.detect(buildAgentConfiguration.getBuildParameters().getEnvironmentVariables());
      myDetector.patchBuildAgentConfiguration(new ConfigurationApplier() {
        @Override
        public void addEnvironmentVariable(String key, String value) {
          buildAgentConfiguration.addEnvironmentVariable(key, value);
        }

        @Override
        public void addConfigurationParameter(String key, String value) {
          buildAgentConfiguration.addConfigurationParameter(key, value);
        }
      }, rvm);
    }
  }

  private class Snapshot implements AgentParametersSupplier {
    private final BuildAgentConfiguration myBuildAgentConfiguration;

    private Snapshot(BuildAgentConfiguration buildAgentConfiguration) {
      myBuildAgentConfiguration = buildAgentConfiguration;
    }
    @Override
    public Map<String, String> getParameters() {
      final Map<String, String> parameters = new HashMap<>();

      @Nullable InstalledRVM rvm = myDetector.detect(myBuildAgentConfiguration.getBuildParameters().getEnvironmentVariables());
      myDetector.patchBuildAgentConfiguration(new ConfigurationApplier() {
        @Override
        public void addEnvironmentVariable(String key, String value) {
          parameters.put(Constants.ENV_PREFIX + key, value);
        }

        @Override
        public void addConfigurationParameter(String key, String value) {
          parameters.put(key, value);
        }
      }, rvm);

      return parameters;
    }
  }
}
