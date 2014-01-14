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

package jetbrains.buildServer.agent.ruby.rbenv;

import java.io.File;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellBasedRubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.ruby.RubyVersionManager;
import jetbrains.buildServer.agent.ruby.impl.RubySdkImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class RbEnvRubySdk extends RubySdkImpl {

  @NotNull private final String myName;
  @NotNull private final InstalledRbEnv myRbEnv;
  @NotNull private final ShellBasedRubyScriptRunner myRubyScriptRunner;

  public RbEnvRubySdk(@NotNull final File home,
                      @NotNull final String name,
                      @NotNull final InstalledRbEnv rbEnv) {
    super(home, null);
    myName = name;
    myRbEnv = rbEnv;
    myRubyScriptRunner = new ShellBasedRubyScriptRunner(new RbEnvShellRunner(rbEnv) {
      @Override
      public String getVersion() {
        return myName;
      }
    });
  }

  public void patchEnvironment(EnvironmentPatchableMap env) {
    env.put(Constants.RBENV_VERSION_ENV_VARIABLE, myName);
  }

  @NotNull
  @Override
  public RubyVersionManager getVersionManager() {
    return myRbEnv;
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  @Override
  public RubyScriptRunner getScriptRunner() {
    return myRubyScriptRunner;
  }
}
