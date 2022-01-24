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

package jetbrains.buildServer.agent.rakerunner.utils;

import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class ConfigurationParamsUtil implements RakeRunnerConstants {

  public static boolean isParameterEnabled(@NotNull final Map<String, String> params, @NotNull final String key) {
    return params.containsKey(key)
           && params.get(key).equals(Boolean.TRUE.toString());
  }

  public static boolean isTraceStagesOptionEnabled(@NotNull final Map<String, String> runParams) {
    return isParameterEnabled(runParams, SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED);
  }

  public static void setParameterEnabled(@NotNull final Map<String, String> runParams,
                                         @NotNull final String frameworkUIProperty,
                                         final boolean isEnabled) {
    runParams.put(frameworkUIProperty, Boolean.valueOf(isEnabled).toString());
  }
}
