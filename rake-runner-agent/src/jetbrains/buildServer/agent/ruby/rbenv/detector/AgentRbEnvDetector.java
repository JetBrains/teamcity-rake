

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.agent.ruby.ConfigurationApplier;
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
                            @NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                            @NotNull final BuildAgentConfiguration buildAgentConfiguration,
                            @NotNull final ExtensionHolder extensionHolder) {
    this.myDetector = detector;
    dispatcher.addListener(new Listener());
    extensionHolder.registerExtension(AgentParametersSupplier.class, getClass().getName(), new Snapshot(buildAgentConfiguration));
    ourInstance = this;
  }

  public static AgentRbEnvDetector getInstance() {
    return ourInstance;
  }

  private class Listener extends AgentLifeCycleAdapter {
    @Override
    public void buildFinished(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
      final BuildAgentConfiguration buildAgentConfiguration = build.getAgentConfiguration();
      @Nullable InstalledRbEnv installedRbEnv = myDetector.detect(buildAgentConfiguration.getBuildParameters().getEnvironmentVariables());
      myDetector.patchBuildAgentConfiguration(new ConfigurationApplier() {
        @Override
        public void addEnvironmentVariable(String key, String value) {
          buildAgentConfiguration.addEnvironmentVariable(key, value);
        }

        @Override
        public void addConfigurationParameter(String key, String value) {
          buildAgentConfiguration.addConfigurationParameter(key, value);
        }
      }, installedRbEnv);
    }
  }

  private class Snapshot implements AgentParametersSupplier {
    @NotNull private final BuildAgentConfiguration myBuildAgentConfiguration;

    private Snapshot(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
      myBuildAgentConfiguration = buildAgentConfiguration;
    }

    @Override
    public Map<String, String> getParameters() {
      final Map<String, String> parameters = new HashMap<>();
      @Nullable InstalledRbEnv installedRbEnv = myDetector.detect(myBuildAgentConfiguration.getBuildParameters().getEnvironmentVariables());
      myDetector.patchBuildAgentConfiguration(new ConfigurationApplier() {
        @Override
        public void addEnvironmentVariable(String key, String value) {
          parameters.put(Constants.ENV_PREFIX + key, value);
        }

        @Override
        public void addConfigurationParameter(String key, String value) {
          parameters.put(key, value);
        }
      }, installedRbEnv);

      return parameters;
    }
  }

}