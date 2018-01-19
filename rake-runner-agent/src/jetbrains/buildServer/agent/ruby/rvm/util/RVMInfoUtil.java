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

package jetbrains.buildServer.agent.ruby.rvm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.ScriptingRunnersProvider;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMInfoUtil {

  public static final Pattern INFO_LINE_PATTERN = Pattern.compile("^[ \\t]*(\\w+):[ \\t]*\"(.*)\"[ \\t]*$");

  @NotNull
  public static RVMInfo gatherInfoUnderRvmShell(@NotNull final String directoryWithRvmrcFile,
                                                @Nullable final Map<String, String> envVariables) {
    final ShellScriptRunner shellScriptRunner = ScriptingRunnersProvider.getRVMDefault().getShellScriptRunner();
    RVMInfo info = new RVMInfo(shellScriptRunner.run("rvm current", directoryWithRvmrcFile, envVariables).getStdout().trim());
    for (RVMInfo.Section section : RVMInfo.Section.values()) {
      final ExecResult output = shellScriptRunner.run("rvm info " + section.name(), directoryWithRvmrcFile, envVariables);
      final String stdout = output.getStdout();
      Map<String, String> ret = new HashMap<String, String>();
      for (String line : stdout.split("\n")) {
        final Matcher matcher = INFO_LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
          continue;
        }
        ret.put(matcher.group(1), matcher.group(2));
      }
      info.setSection(section, ret);
    }
    return info;
  }

}
