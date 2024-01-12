

package jetbrains.buildServer.agent.ruby;

public interface ConfigurationApplier {
  void addEnvironmentVariable(String key, String value);
  void addConfigurationParameter(String key, String value);
}