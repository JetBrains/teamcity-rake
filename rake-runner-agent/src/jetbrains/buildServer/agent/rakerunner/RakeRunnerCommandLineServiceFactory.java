package jetbrains.buildServer.agent.rakerunner;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Pavel.Sher
 */
public class RakeRunnerCommandLineServiceFactory implements CommandLineBuildServiceFactory {
  @NotNull
  public CommandLineBuildService createService() {
    return new RakeTasksBuildService();
  }

  @NotNull
  public AgentBuildRunnerInfo getBuildRunnerInfo() {
    return new AgentBuildRunnerInfo() {
      @NotNull
      public String getType() {
        return RakeRunnerConstants.RUNNER_TYPE;
      }

      public boolean canRun(@NotNull final BuildAgentConfiguration buildAgentConfiguration) {
        return true;
      }
    };
  }
}
