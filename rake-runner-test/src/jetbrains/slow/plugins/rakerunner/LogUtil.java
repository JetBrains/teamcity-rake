/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.slow.plugins.rakerunner;

import java.util.concurrent.atomic.AtomicReference;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.FlowLogger;
import jetbrains.buildServer.agent.NullBuildProgressLogger;
import jetbrains.buildServer.agent.ServerLoggerFacade;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.testng.internal.collections.Pair;

public class LogUtil {
  private static final AtomicReference<Pair<Logger, FlowLogger>> ourCachedFlowLogger = new AtomicReference<Pair<Logger, FlowLogger>>();

  /**
   * Provides FlowLogger.
   * When runs under TeamCity - ServerLoggerFacade used, fallback lo 'log' otherwise.
   * @param log fallback logger
   * @return see above
   */
  @NotNull
  public static FlowLogger getFlowLogger(final Logger log) {
    Pair<Logger, FlowLogger> pair = ourCachedFlowLogger.get();
    if (pair != null && pair.second() != null) {
      if (pair.first() == null || log == pair.first()) return pair.second();
    }
    synchronized (ourCachedFlowLogger) {
      pair = ourCachedFlowLogger.get();
      if (pair != null && pair.second() != null) {
        if (pair.first() == null || log == pair.first()) return pair.second();
      }
      final String buildId = AgentRuntimeProperties.getBuildId();
      if (buildId == null) {
        pair = Pair.<Logger, FlowLogger>create(null, new LoggerToBuildProgressLoggerAdapter(log));
      } else {
        pair = Pair.<Logger, FlowLogger>create(log, new ServerLoggerFacade(buildId));
      }
      ourCachedFlowLogger.set(pair);
      return pair.second();
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
