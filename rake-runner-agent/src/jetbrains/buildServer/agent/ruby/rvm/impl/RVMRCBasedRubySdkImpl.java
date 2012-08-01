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

package jetbrains.buildServer.agent.ruby.rvm.impl;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.scripting.*;
import jetbrains.buildServer.agent.rakerunner.utils.EnvUtil;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import jetbrains.buildServer.agent.ruby.rvm.RVMRCBasedRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.util.RVMInfoUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRCBasedRubySdkImpl extends RVMRubySdkImpl implements RVMRCBasedRubySdk {

  private static final String TEST_RVM_SHELL_SCRIPT = ". $rvm_path/scripts/rvm && cd %s && rvm current";
  private static final Logger LOG = Logger.getInstance(RVMRCBasedRubySdkImpl.class.getName());
  private static final Map<SdkDescriptor, RVMRCBasedRubySdkImpl> ourCache = new HashMap<SdkDescriptor, RVMRCBasedRubySdkImpl>();

  @NotNull
  private final String myPathToRVMRCFolder;
  private final ShellBasedRubyScriptRunner myShellBasedRubyScriptRunner;

  public static void cache(final RVMRCBasedRubySdkImpl sdk, final Map<String, String> env) {
    ourCache.put(new SdkDescriptor(sdk.myPathToRVMRCFolder, env), sdk);
  }

  public static void clearCache() {
    ourCache.clear();
  }

  public static RVMRCBasedRubySdkImpl getOrCreate(@NotNull final String pathToRVMRCFolder, @NotNull final Map<String, String> env)
    throws RakeTasksBuildService.MyBuildFailureException {
    final RVMRCBasedRubySdkImpl cached = ourCache.get(new SdkDescriptor(pathToRVMRCFolder, env));
    if (cached != null) {
      return cached;
    }
    final RVMRCBasedRubySdkImpl sdk = createAndSetup(pathToRVMRCFolder, Collections.unmodifiableMap(env));
    cache(sdk, env);
    return sdk;
  }

  private static RVMRCBasedRubySdkImpl createAndSetup(@NotNull final String pathToRVMRCFolder,
                                                     @NotNull final Map<String, String> env)
    throws RakeTasksBuildService.MyBuildFailureException {
    final ShellScriptRunner shellScriptRunner = ScriptingRunnersProvider.getRVMDefault().getShellScriptRunner();
    final RunnerUtil.Output testRun = shellScriptRunner.run(String.format(TEST_RVM_SHELL_SCRIPT, pathToRVMRCFolder), pathToRVMRCFolder, env);
    if (!StringUtil.isEmptyOrSpaces(testRun.getStderr())) {
      StringBuilder sb = new StringBuilder();
      sb.append("Configuring RVM with ").append(pathToRVMRCFolder).append("/.rvmrc failed:");
      sb.append("\nStdOut: ").append(testRun.getStdout());
      sb.append("\nStdErr: ").append(testRun.getStderr());
      throw new RakeTasksBuildService.MyBuildFailureException(sb.toString());
    }
    final RVMInfo info = RVMInfoUtil.gatherInfoUnderRvmShell(pathToRVMRCFolder, env);

    // Constructor params
    final String interpreterPath = info.getSection(RVMInfo.Section.binaries).get("ruby");
    final String gemset = info.getSection(RVMInfo.Section.environment).get("gemset");
    final String name = info.getInterpreterName();
    final boolean isSystem = RVMSupportUtil.RVM_SYSTEM_INTERPRETER.equals(name);

    if (LOG.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("\n\t").append("Configuring interpreter with .rvmrc");
      sb.append("\n\t").append("Interpreter Path is ").append(interpreterPath);
      sb.append("\n\t").append("Gemset is ").append(gemset);
      sb.append("\n\t").append("Name is ").append(name);
      sb.append("\n\t").append("IsSystem = ").append(isSystem);
      sb.append("\n\t").append("PathToRVMRCFolder = ").append(pathToRVMRCFolder);
      LOG.debug(sb.toString());
    }

    return new RVMRCBasedRubySdkImpl(interpreterPath, name, isSystem, gemset, pathToRVMRCFolder);
  }

  private RVMRCBasedRubySdkImpl(@NotNull final String interpreterPath,
                                @NotNull final String name,
                                final boolean system,
                                @Nullable final String gemset,
                                @NotNull final String pathToRVMRCFolder) {
    super(interpreterPath, name, system, gemset);
    myPathToRVMRCFolder = pathToRVMRCFolder;
    myShellBasedRubyScriptRunner = new ShellBasedRubyScriptRunner(new MyRvmShellRunner());
  }

  @Override
  public void setup(@NotNull final Map<String, String> env) {
    if (isSetupCompleted()) {
      return;
    }

    // 1.8 / 1.9
    setIsRuby19(InternalRubySdkUtil.isRuby19Interpreter(this, env));

    // ruby / jruby
    setIsJRuby(InternalRubySdkUtil.isJRubyInterpreter(this, env));

    // gem paths
    setGemPathsLog(InternalRubySdkUtil.getGemPaths(this, env));

    // load path
    setLoadPathsLog(InternalRubySdkUtil.getLoadPaths(this, env));

    // Set setup completed
    setIsSetupCompleted(true);
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
    public RunnerUtil.Output run(@NotNull final String script,
                                 @NotNull final String workingDirectory,
                                 @Nullable final Map<String, String> environment) {
      StringBuilder sb = new StringBuilder();
      sb.append("cd ").append(workingDirectory).append('\n');
      sb.append(script);

      return super.run(sb.toString(), myPathToRVMRCFolder, environment);
    }
  }
}
