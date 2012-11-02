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

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;

/**
 * Run shell script under rvm-shell.
 * Produces special .sh script file and runs it.
 * <p/>
 * RVM required.
 * Unix/Linux only.
 *
 * @author Vladislav.Rassokhin
 */
public class RvmShellRunner implements ShellScriptRunner {
  private static final Logger LOG = Logger.getInstance(RvmShellRunner.class.getName());
  private static RvmShellRunner ourRvmShellRunner;
  private final InstalledRVM myRVM;

  public InstalledRVM getRVM() {
    return myRVM;
  }

  public RvmShellRunner(@NotNull final InstalledRVM rvm) {
    myRVM = rvm;
  }

  @NotNull
  static RvmShellRunner getRvmShellRunner() {
    final InstalledRVM rvm = RVMPathsSettings.getRVMNullSafe();
    if (ourRvmShellRunner == null || !ourRvmShellRunner.getRVM().equals(rvm)) {
      ourRvmShellRunner = new RvmShellRunner(rvm);
    }
    return ourRvmShellRunner;
  }

  /**
   * Run script.
   *
   * @param script           script
   * @param workingDirectory directory where .rvmrc exists
   * @return script output
   */
  @NotNull
  public RunnerUtil.Output run(@NotNull final String script,
                               @NotNull final String workingDirectory,
                               @Nullable final Map<String, String> environment) {
    final File directory = new File(workingDirectory);
    File scriptFile = null;
    try {
      try {
        scriptFile = FileUtil.createTempFile(directory, "rvm_shell", ".sh", true);
        FileUtil.writeFileAndReportErrors(scriptFile, script);
      } catch (IOException e) {
        LOG.error("Failed to create temp file, error: ", e);
        return new RunnerUtil.Output("", "Failed to create temp file, error: " + e.getMessage());
      }

      // Patching environment
      final HashMap<String, String> environment1 = new HashMap<String, String>();
      if (environment != null) {
        environment1.putAll(environment);
      }
      environment1.put("rvm_trust_rvmrcs_flag", "1");
      environment1.put("rvm_path", myRVM.getPath());

      return RunnerUtil.run(workingDirectory, environment1,
                            createProcessArguments(myRVM.getPath() + "/bin/rvm-shell", workingDirectory, scriptFile));
    } finally {
      try {
        if (scriptFile != null) {
          FileUtil.delete(scriptFile);
        }
      } catch (SecurityException ignored) {
      }
    }
  }

  @NotNull
  protected String[] createProcessArguments(@NotNull final String rvmShellEx,
                                            @NotNull final String workingDirectory,
                                            @NotNull final File scriptFile) {
    return new String[]{rvmShellEx, "--path", workingDirectory, scriptFile.getAbsolutePath()};
  }
}
