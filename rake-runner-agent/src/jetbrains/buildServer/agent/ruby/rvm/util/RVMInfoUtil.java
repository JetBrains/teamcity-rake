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

package jetbrains.buildServer.agent.ruby.rvm.util;

import com.intellij.openapi.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.rakerunner.utils.RubyScriptRunner;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMInfoUtil {

  public static final Pattern INFO_LINE_PATTERN = Pattern.compile("^[ \\t]*(\\w+):[ \\t]*\"(\\w*)\"[ \\t]*$");

  @NotNull
  public static List<Pair<String, String>> parseOutput(@NotNull final RubyScriptRunner.Output output) {
    final String stdout = output.getStdout();
    final String[] lines = stdout.split("\n");

    final List<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
    for (String line : lines) {
      final Matcher matcher = INFO_LINE_PATTERN.matcher(line);
      if (!matcher.find()) {
        continue;
      }
      ret.add(new Pair<String, String>(matcher.group(1), matcher.group(2)));
    }
    return ret;
  }

  @Nullable
  public static String getInfoString(@NotNull final List<Pair<String, String>> infos, @NotNull final String key) {
    for (Pair<String, String> info : infos) {
      if (key.equals(info.first)) {
        return info.second;
      }
    }
    return null;
  }

  @NotNull
  public static RubyScriptRunner.Output runRvmInfoUnderRvmShell(@NotNull final String workingDirectory) {
    final InstalledRVM rvm = RVMPathsSettings.getInstance().getRVM();
    if (rvm == null) {
      throw new IllegalArgumentException("RVM home unkown.");
    }
    return RubyScriptRunner.runUnderRvmShell(workingDirectory, rvm.getPath() + "/bin/rvm", "info", "ruby,binaries,environment");
  }

}
