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

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Vladislav.Rassokhin
 */
public class ShellScriptRunner {
  private static final Logger LOG = Logger.getInstance(ShellScriptRunner.class.getName());

  /**
   * Run shell script under rvm-shell.
   * Produces special .sh script file and runs it.
   *
   * @param pathToRVMRCFolder directory where .rvmrc exists
   * @param script            script
   * @return script output
   */
  @NotNull
  public static RunnerUtil.Output runUnderRvmShell(@NotNull final String pathToRVMRCFolder, @NotNull final String script) {

    final InstalledRVM rvm = RVMPathsSettings.getInstance().getRVM();
    if (rvm == null) {
      throw new IllegalArgumentException("RVM home unkown.");
    }


    final File directory = new File(pathToRVMRCFolder);
    File scriptFile;
    try {
      scriptFile = File.createTempFile("rvm_shell", ".sh", directory);
      StringBuilder content = new StringBuilder();
      content.append("#!").append(rvm.getPath()).append("/bin/rvm-shell").append('\n');
      content.append(script);
      jetbrains.buildServer.util.FileUtil.writeFile(scriptFile, content.toString());
      makeScriptFileExecutable(scriptFile); // script needs to be made executable for all (chmod a+x)
    } catch (IOException e) {
      LOG.error("Failed to create temp file, error: ", e);
      return new RunnerUtil.Output("", "Failed to create temp file, error: " + e.getMessage());
    }

    // Patching environment
    final HashMap<String, String> environment = new HashMap<String, String>();
    environment.put("rvm_trust_rvmrcs_flag", "1");
    environment.put("rvm_path", rvm.getPath());

    return RunnerUtil.run(pathToRVMRCFolder, environment, scriptFile.getAbsolutePath());
  }

  private static void makeScriptFileExecutable(@NotNull final File scriptFile) throws IOException {
    setPermissions(scriptFile, "u+x");
  }

  private static void setPermissions(@NotNull final File script, @NotNull final String perms) throws IOException {
    Process process = Runtime.getRuntime().exec(new String[]{"chmod", perms, script.getAbsolutePath()});
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      LOG.error("Failed to execute chmod " + perms + " " + script.getAbsolutePath() + ", error: " + e.toString());
    }
  }
}
