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

package jetbrains.buildServer.agent.ruby.rvm.impl;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.scripting.*;
import jetbrains.buildServer.agent.rakerunner.utils.EnvUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import jetbrains.buildServer.agent.ruby.rvm.util.RVMInfoUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRCBasedRubySdkImpl extends RVMRubySdkImpl {

  private static final String TEST_RVM_SHELL_SCRIPT = ". $rvm_path/scripts/rvm && cd %s && rvm current";
  private static final Logger LOG = Logger.getInstance(RVMRCBasedRubySdkImpl.class.getName());
  private static final Map<SdkDescriptor, RubySdk> ourCache = new HashMap<SdkDescriptor, RubySdk>();

  @NotNull
  private final String myPathToRVMRCFolder;
  private final ShellBasedRubyScriptRunner myShellBasedRubyScriptRunner;

  public static void cache(final RubySdk sdk, final Map<String, String> env, final String pathToRVMRCFolder) {
    ourCache.put(new SdkDescriptor(pathToRVMRCFolder, env), sdk);
  }

  public static void clearCache() {
    ourCache.clear();
  }

  public static RubySdk getOrCreate(@NotNull final String pathToRVMRCFolder, @NotNull final Map<String, String> env)
    throws RakeTasksBuildService.MyBuildFailureException {
    final RubySdk cached = ourCache.get(new SdkDescriptor(pathToRVMRCFolder, env));
    if (cached != null) {
      return cached;
    }
    final RubySdk sdk = createAndSetup(pathToRVMRCFolder, Collections.unmodifiableMap(env));
    cache(sdk, env, pathToRVMRCFolder);
    return sdk;
  }

  private static RubySdk createAndSetup(@NotNull final String pathToRVMRCFolder, @NotNull final Map<String, String> env)
    throws RakeTasksBuildService.MyBuildFailureException {
    final ShellScriptRunner shellScriptRunner = ScriptingRunnersProvider.getRVMDefault().getShellScriptRunner();
    final ExecResult testRun = shellScriptRunner.run(String.format(TEST_RVM_SHELL_SCRIPT, pathToRVMRCFolder), pathToRVMRCFolder, env);
    //noinspection ThrowableResultOfMethodCallIgnored
    if (testRun.getExitCode() != 0 || testRun.getException() != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("Configuring RVM with ").append(pathToRVMRCFolder).append("/.rvmrc failed:");
      sb.append(testRun.toString());
      throw new RakeTasksBuildService.MyBuildFailureException(sb.toString());
    }
    final RVMInfo info = RVMInfoUtil.gatherInfoUnderRvmShell(pathToRVMRCFolder, env);

    // Constructor params
    final String name = info.getInterpreterName();

    if (RVMSupportUtil.isSystemRuby(name)) {
      final String executablePath = info.getSection(RVMInfo.Section.binaries).get("ruby");

      if (LOG.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t").append("Configuring system interpreter with .rvmrc");
        sb.append("\n\t").append("PathToRVMRCFolder = ").append(pathToRVMRCFolder);
        sb.append("\n\t").append("Executable Path is ").append(executablePath);
        LOG.debug(sb.toString());
      }

      return new RVMRubySdkImpl(new File(executablePath));
    } else {
      final String gemset = info.getSection(RVMInfo.Section.environment).get("gemset");

      if (LOG.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t").append("Configuring rvm interpreter with .rvmrc");
        sb.append("\n\t").append("PathToRVMRCFolder = ").append(pathToRVMRCFolder);
        sb.append("\n\t").append("Gemset is ").append(gemset);
        sb.append("\n\t").append("Name is ").append(name);
        LOG.debug(sb.toString());
      }

      final File home = RVMPathsSettings.getRVMNullSafe().getHomeForVersionName(name);
      if (home == null) {
        throw new RakeTasksBuildService.MyBuildFailureException(String.format("Cannot find home path for RVM SDK with name %s", name));
      }
      return new RVMRCBasedRubySdkImpl(home, name, gemset, pathToRVMRCFolder);
    }
  }

  private RVMRCBasedRubySdkImpl(@NotNull final File home, @NotNull final String name,
                                @Nullable final String gemset,
                                @NotNull final String pathToRVMRCFolder) {
    super(home, name, gemset);
    myPathToRVMRCFolder = pathToRVMRCFolder;
    myShellBasedRubyScriptRunner = new ShellBasedRubyScriptRunner(new MyRvmShellRunner());
  }

  @NotNull
  public String getPathToRVMRCFolder() {
    return myPathToRVMRCFolder;
  }

  @NotNull
  @Override
  public RubyScriptRunner getScriptRunner() {
    return myShellBasedRubyScriptRunner;
  }

  private static final class SdkDescriptor {
    private final String myPathToRVMRCFolder;
    private final Map<String, String> myEnv;

    private SdkDescriptor(@NotNull final String pathToRVMRCFolder, @NotNull final Map<String, String> env) {
      myPathToRVMRCFolder = pathToRVMRCFolder;
      myEnv = EnvUtil.getCompactEnvMap(env);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final SdkDescriptor that = (SdkDescriptor)o;

      return myPathToRVMRCFolder.equals(that.myPathToRVMRCFolder) && myEnv.equals(that.myEnv);
    }

    @Override
    public int hashCode() {
      return 31 * myPathToRVMRCFolder.hashCode() + myEnv.hashCode();
    }
  }

  private class MyRvmShellRunner extends RvmShellRunner {
    public MyRvmShellRunner() {
      super(RVMPathsSettings.getRVMNullSafe());
    }

    @NotNull
    @Override
    public ExecResult run(@NotNull final String script,
                          @NotNull final String workingDirectory,
                          @Nullable final Map<String, String> environment) {
      StringBuilder sb = new StringBuilder();
      sb.append("cd ").append(workingDirectory).append('\n');
      sb.append(script);

      return super.run(sb.toString(), myPathToRVMRCFolder, environment);
    }
  }
}
