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

package jetbrains.buildServer.agent.ruby.impl;

import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.scripting.ProcessBasedRubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public class RubySdkImpl implements RubySdk {

  private final String myInterpreterPath;
  private final boolean myIsSystem;

  private boolean myIsRuby19;
  private boolean myIsJRuby;
  private String[] myGemPaths;
  private String[] myLoadPaths;
  private RunnerUtil.Output myGemPathsLog;
  private RunnerUtil.Output myLoadPathsLog;
  private boolean myIsSetupCompleted = false;

  public RubySdkImpl(@NotNull final String interpreterPath, boolean isSystem) {
    myInterpreterPath = interpreterPath;
    myIsSystem = isSystem;
  }

  @NotNull
  public String[] getGemPaths() {
    return myGemPaths;
  }

  public boolean isRuby19() {
    return myIsRuby19;
  }

  public boolean isJRuby() {
    return myIsJRuby;
  }

  @NotNull
  public RunnerUtil.Output getGemPathsFetchLog() {
    return myGemPathsLog;
  }

  @NotNull
  public RunnerUtil.Output getLoadPathsFetchLog() {
    return myLoadPathsLog;
  }

  @NotNull
  public RubyScriptRunner getScriptRunner() {
    return new ProcessBasedRubyScriptRunner(this);
  }

  @NotNull
  public String[] getLoadPath() {
    return myLoadPaths;
  }

  //public ProgramCommandLine createProgramCommandLineForScript(@NotNull final String workingDirectory,
  //                                                            @NotNull final String[] rubyArgs,
  //                                                            @Nullable final Map<String, String> buildConfEnvironment,
  //                                                            @NotNull final String scriptFilePath,
  //                                                            @NotNull final String... scriptArgs)
  //  throws RakeTasksBuildService.MyBuildFailureException {
  //  final List<String> arguments = new ArrayList<String>();
  //  HashMap<String, String> processEnv = new HashMap<String, String>();
  //  if (buildConfEnvironment != null) {
  //    processEnv.putAll(buildConfEnvironment);
  //  }
  //
  //  try {
  //    //// Writing source to the temp file
  //    //File scriptFile = File.createTempFile("script", ".rb");
  //    //FileUtil.writeFile(scriptFile, scriptSource);
  //    //// Autodelete file on exit
  //    //FileUtil.writeFile(scriptFile, getRemoveFileRubyScript(scriptFile));
  //
  //    //Args
  //    Collections.addAll(arguments, rubyArgs);
  //    arguments.add(scriptFilePath);
  //    Collections.addAll(arguments, scriptArgs);
  //
  //    // Env
  //    // TODO: replace it
  //    RVMSupportUtil.patchEnvForRVMIfNecessary(this, processEnv);
  //
  //    return new SimpleProgramCommandLine(processEnv, workingDirectory, getInterpreterPath(), arguments);
  //  } catch (Exception e) {
  //    throw new RakeTasksBuildService.MyBuildFailureException(e.getMessage());
  //  }
  //}

  //TODO: move to appropriate place
  //private static String getRemoveFileRubyScript(final File scriptFilePath) {
  //  return "\n\n\nFile.delete('" + scriptFilePath.getAbsolutePath() + "')\n\n";
  //}

  public void setIsRuby19(final boolean isRuby19) {
    myIsRuby19 = isRuby19;
  }

  public void setIsJRuby(final boolean isJRuby) {
    myIsJRuby = isJRuby;
  }

  public void setGemPathsLog(final RunnerUtil.Output gemPathsLog) {
    myGemPathsLog = gemPathsLog;
    myGemPaths = TextUtil.splitByLines(gemPathsLog.getStdout());

  }

  public void setLoadPathsLog(final RunnerUtil.Output loadPathsLog) {
    myLoadPathsLog = loadPathsLog;
    myLoadPaths = TextUtil.splitByLines(loadPathsLog.getStdout());
  }

  @NotNull
  public String getInterpreterPath() {
    return myInterpreterPath;
  }

  public boolean isRvmSdk() {
    return false;
  }

  public boolean isSystem() {
    return myIsSystem;
  }

  @NotNull
  public String getPresentableName() {
    return myInterpreterPath;
  }

  public boolean isSetupCompleted() {
    return myIsSetupCompleted;
  }

  public void setIsSetupCompleted(final boolean isSetupCompleted) {
    myIsSetupCompleted = isSetupCompleted;
  }

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
}
