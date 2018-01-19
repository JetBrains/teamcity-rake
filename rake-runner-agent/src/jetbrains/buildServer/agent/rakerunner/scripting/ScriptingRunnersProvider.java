/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner.scripting;

import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class ScriptingRunnersProvider {
  private static ScriptingRunnersProvider ourRVMDefaultRunnersProvider;

  @NotNull
  public abstract RubyScriptRunner getRubyScriptRunner();

  @NotNull
  public abstract ShellScriptRunner getShellScriptRunner();

  public static final ScriptingRunnersProvider RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER;

  static {
    RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER = new ScriptingRunnersProvider() {
      @NotNull
      @Override
      public RubyScriptRunner getRubyScriptRunner() {
        return new ShellBasedRubyScriptRunner(getShellScriptRunner());
      }

      @NotNull
      @Override
      public ShellScriptRunner getShellScriptRunner() {
        return RvmShellRunner.getRvmShellRunner();
      }
    };
    ourRVMDefaultRunnersProvider = RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER;
  }

  @NotNull
  public static ScriptingRunnersProvider getRVMDefault() {
    return ourRVMDefaultRunnersProvider;
  }

  public static void setRVMDefault(@NotNull final ScriptingRunnersProvider runnersProvider) {
    ourRVMDefaultRunnersProvider = runnersProvider;
  }
}
