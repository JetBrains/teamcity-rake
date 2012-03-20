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

package jetbrains.buildServer.agent.ruby.rvm;

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.BuildCommandLineProcessor;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConfiguration;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMCommandLineProcessor implements BuildCommandLineProcessor {

  public static final String CUSTOM_EXECUTABLE = "/bin/sh";
  public static final String SCRIPT_PERMISSIONS = "u+x";

  @NotNull
  public ProgramCommandLine process(@NotNull final BuildRunnerContext runnerContext, @NotNull final ProgramCommandLine origCommandLine)
    throws RunBuildException {
    if (!SystemInfo.isUnix) {
      return origCommandLine;
    }

    final AgentRunningBuild build = runnerContext.getBuild();
    final RubyEnvConfiguratorConfiguration configuration = new RubyEnvConfiguratorConfiguration(build.getSharedConfigParameters());

    if (configuration.getType() != RubyEnvConfiguratorConfiguration.Type.RVM &&
        configuration.getType() != RubyEnvConfiguratorConfiguration.Type.RVMRC) {
      return origCommandLine;
    }

    // Lets patch it!

    final File script = createScriptFile(origCommandLine, build.getAgentTempDirectory());

    return new ProgramCommandLine() {
      @NotNull
      public String getExecutablePath() {
        return CUSTOM_EXECUTABLE;
      }

      @NotNull
      public String getWorkingDirectory() throws RunBuildException {
        return origCommandLine.getWorkingDirectory();
      }

      @NotNull
      public List<String> getArguments() {
        return Collections.singletonList(script.getAbsolutePath());
      }

      @NotNull
      public Map<String, String> getEnvironment() throws RunBuildException {
        return origCommandLine.getEnvironment();
      }
    };
  }

  private static File createScriptFile(@NotNull final ProgramCommandLine origCommandLine, @NotNull final File directory)
    throws RunBuildException {
    final File script;
    try {
      script = File.createTempFile("build", ".sh", directory);
      StringBuilder content = new StringBuilder();
      content.append("cd ").append(origCommandLine.getWorkingDirectory()).append("\n");
      content.append(createOriginalCommandLine(origCommandLine));
      FileUtil.writeFile(script, content.toString());

      setPermissions(script, SCRIPT_PERMISSIONS); // script needs to be made executable for all (chmod a+x)
    } catch (IOException e) {
      throw new RunBuildException("Failed to create temp file, error: " + e.toString());
    }
    return script;
  }

  private static void setPermissions(@NotNull final File script, @NotNull final String perms) throws IOException {
    Process process = Runtime.getRuntime().exec(new String[]{"chmod", perms, script.getAbsolutePath()});
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      Loggers.AGENT.warn("Failed to execute chmod " + perms + " " + script.getAbsolutePath() + ", error: " + e.toString());
    }
  }

  private static String createOriginalCommandLine(@NotNull final ProgramCommandLine commandLine) throws RunBuildException {
    StringBuilder sb = new StringBuilder();
    sb.append(commandLine.getExecutablePath());
    for (String arg : commandLine.getArguments()) {
      sb.append(" ");
      final boolean hasSpaces = arg.indexOf(' ') != -1;
      if (hasSpaces) {
        sb.append("\"");
      }
      sb.append(StringUtil.escapeQuotes(arg));
      if (hasSpaces) {
        sb.append("\"");
      }
    }
    return sb.toString();
  }
}
