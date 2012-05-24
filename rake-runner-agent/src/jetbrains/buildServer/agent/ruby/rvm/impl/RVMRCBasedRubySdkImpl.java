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
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.RvmShellRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ScriptingRunnersProvider;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellBasedRubyScriptRunner;
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

  private static final Logger LOG = Logger.getInstance(RVMRCBasedRubySdkImpl.class.getName());
  private final String myPathToRVMRCFolder;

  public static RVMRCBasedRubySdkImpl createAndSetup(@NotNull final String pathToRVMRCFolder,
                                                     @NotNull final Map<String, String> envVariables)
    throws RakeTasksBuildService.MyBuildFailureException {
    final RunnerUtil.Output testRun =
      ScriptingRunnersProvider.getRVMDefault().getShellScriptRunner().run("rvm current", pathToRVMRCFolder, null);
    if (!StringUtil.isEmptyOrSpaces(testRun.getStderr())) {
      StringBuilder sb = new StringBuilder();
      sb.append("Configuring RVM with ").append(pathToRVMRCFolder).append("/.rvmrc failed:");
      sb.append("\nStdOut: ").append(testRun.getStdout());
      sb.append("\nStdErr: ").append(testRun.getStderr());
      throw new RakeTasksBuildService.MyBuildFailureException(sb.toString());
    }
    final RVMInfo info = RVMInfoUtil.gatherInfoUnderRvmShell(pathToRVMRCFolder, envVariables);

    // Constructor params
    final String interpreterPath = info.getSection(RVMInfo.Section.binaries).get("ruby");
    final String gemset = info.getSection(RVMInfo.Section.environment).get("gemset");
    final String name = info.getInterpreterName();
    final boolean isSystem = RVMSupportUtil.RVM_SYSTEM_INTERPRETER.equals(name);

    LOG.debug("Configuring interpreter with .rvmrc");
    LOG.debug("Interpreter Path is " + interpreterPath);
    LOG.debug("Gemset is " + gemset);
    LOG.debug("Name is " + name);
    LOG.debug("IsSystem = " + isSystem);
    LOG.debug("PathToRVMRCFolder = " + pathToRVMRCFolder);

    return new RVMRCBasedRubySdkImpl(interpreterPath, name, isSystem, gemset, pathToRVMRCFolder);
  }

  private RVMRCBasedRubySdkImpl(@NotNull final String interpreterPath,
                                final String name,
                                final boolean system,
                                final String gemset,
                                final String pathToRVMRCFolder) {
    super(interpreterPath, name, system, gemset);
    myPathToRVMRCFolder = pathToRVMRCFolder;
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
    //noinspection ConstantConditions
    return new ShellBasedRubyScriptRunner(new RvmShellRunner(RVMPathsSettings.getInstance().getRVM())) {
      @NotNull
      @Override
      public RunnerUtil.Output run(@NotNull final String script,
                                   @NotNull final String workingDirectory,
                                   @Nullable final Map<String, String> environment,
                                   @NotNull final String... rubyArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("cd ").append(workingDirectory).append('\n');
        sb.append(script);

        return super.run(sb.toString(), myPathToRVMRCFolder, environment, rubyArgs);
      }
    };
    // TODO: use SRP
    //return ScriptingRunnersProvider.getRVMDefault().getRubyScriptRunner();
  }
}
