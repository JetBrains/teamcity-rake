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

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import java.util.Map;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.ruby.rbenv.Constants;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for "rbenv Detector" - an utility designed for
 * detecting rbenv installation on agent.
 *
 * @author Vladislav.Rassokhin
 */
public abstract class RbEnvDetector {

  /**
   * That function detects installed rbenv.
   *
   * @param environmentParams environment variables map
   * @return founded rbenv installation or null if rbenv does not found
   */
  @Nullable
  public abstract InstalledRbEnv detect(@NotNull final Map<String, String> environmentParams);

  public void patchBuildAgentConfiguration(@NotNull final BuildAgentConfiguration configuration, @Nullable final InstalledRbEnv rbenv) {
    if (rbenv == null) {
      return;
    }

    configuration.addEnvironmentVariable(Constants.RBENV_ROOT_ENV_VARIABLE, rbenv.getHome().getAbsolutePath());

    // TODO: do not provide this parameter, install ruby using (ruby-build) if necessary
    String allVersions = StringUtil.join(",", rbenv.getInstalledVersions());
    configuration.addConfigurationParameter(Constants.CONF_RBENV_RUBIES_LIST, allVersions);
  }
}
