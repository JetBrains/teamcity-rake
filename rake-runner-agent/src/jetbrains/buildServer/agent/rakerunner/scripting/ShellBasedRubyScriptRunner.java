/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class ShellBasedRubyScriptRunner implements jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner {
  private final ShellScriptRunner myShellScriptRunner;

  ShellBasedRubyScriptRunner(@NotNull final ShellScriptRunner shellScriptRunner) {
    myShellScriptRunner = shellScriptRunner;
  }

  @NotNull
  public RunnerUtil.Output run(@NotNull final String script, @NotNull final String workingDirectory) {
    return myShellScriptRunner.run(rubyScriptToShellScript(script), workingDirectory);
  }

  @NotNull
  public RunnerUtil.Output run(@NotNull final String script, @NotNull final String workingDirectory, @NotNull final String... rubyArgs) {
    return myShellScriptRunner.run(rubyScriptToShellScript(script, rubyArgs), workingDirectory);
  }

  public static String rubyScriptToShellScript(@NotNull final String rubyScript, @NotNull String... rubyArgs) {
    final StringBuilder sb = new StringBuilder();
    sb.append("ruby");
    for (String rubyArg : rubyArgs) {
      sb.append(' ').append(rubyArg);
    }
    sb.append(" -e ");
    sb.append('"').append(StringUtil.quoteReplacement(rubyScript)).append('"');
    return sb.toString();
  }

}
