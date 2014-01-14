/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import java.util.Map;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for "RVM Detector" - an utility designed for
 * detecting RVM installation on agent.
 *
 * @author Vladislav.Rassokhin
 */
public abstract class RVMDetector {

  public static final String CONF_PARAMETER_PREFIX = "rvm.";
  public static final String CONF_RVM_RUBIES_LIST = CONF_PARAMETER_PREFIX + "rubies.list";
  public static final String RVM_PATH_ENV_VARIABLE = "rvm_path";

  /**
   * That function detects installed RVM.
   *
   * @param environmentParams environment variables map
   * @return founded RVM installation or null if RVM does not found
   */
  @Nullable
  public abstract InstalledRVM detect(@NotNull final Map<String, String> environmentParams);

  public void patchBuildAgentConfiguration(@NotNull final BuildAgentConfiguration configuration, @Nullable final InstalledRVM rvm) {
    if (rvm == null) {
      return;
    }

    configuration.addEnvironmentVariable(RVM_PATH_ENV_VARIABLE, rvm.getPath());

    // TODO: do not provide this parameter, install ruby if necessary
    String allVersions = StringUtil.join(",", rvm.getRubiesNames());
    configuration.addConfigurationParameter(CONF_RVM_RUBIES_LIST, allVersions);
  }
}
