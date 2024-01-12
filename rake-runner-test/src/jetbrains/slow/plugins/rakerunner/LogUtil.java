

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.FlowLogger;
import jetbrains.buildServer.agent.NullBuildProgressLogger;
import jetbrains.buildServer.agent.ServerLoggerFacade;
import jetbrains.buildServer.agent.impl.ServerLogger;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class LogUtil {
  /**
   * Provides FlowLogger.
   * When runs under TeamCity - ServerLoggerFacade used, fallback lo 'log' otherwise.
   *
   * @param log fallback logger
   * @return see above
   */
  @NotNull
  public static FlowLogger getFlowLogger(final Logger log) {
    final String buildId = AgentRuntimeProperties.getBuildId();
    if (buildId == null) {
      return new LoggerToBuildProgressLoggerAdapter(log);
    } else {
      return new ServerLoggerFacade(ServerLogger.getLoggerInstance(buildId, AbstractRakeRunnerTest.getAgentOwnPort()), buildId);
    }
  }

  private static class LoggerToBuildProgressLoggerAdapter extends NullBuildProgressLogger {
    private final Logger myLog;

    public LoggerToBuildProgressLoggerAdapter(final Logger log) {
      myLog = log;
    }

    @Override
    public void activityStarted(final String activityName, final String activityType) {
      myLog.info("Activity started: " + activityName);
    }

    @Override
    public void activityFinished(final String activityName, final String activityType) {
      myLog.info("Activity finished: " + activityName);
    }

    @Override
    public void message(final String message) {
      myLog.info(message);
    }

    @Override
    public void error(final String message) {
      myLog.error(message);
    }
  }
}