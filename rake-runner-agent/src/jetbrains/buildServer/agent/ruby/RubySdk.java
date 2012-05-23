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

package jetbrains.buildServer.agent.ruby;

import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public interface RubySdk {
  @NotNull
  String getInterpreterPath();

  @NotNull
  String getPresentableName();

  boolean isRvmSdk();

  boolean isSystem();

  boolean isRuby19();

  boolean isJRuby();

  @NotNull
  String[] getGemPaths();

  @NotNull
  String[] getLoadPath();

  boolean isSetupCompleted();

  void setup(@NotNull final Map<String, String> env);

  //ProgramCommandLine createProgramCommandLineForScript(@NotNull final String workingDirectory,
  //                                                     @NotNull final String[] rubyArgs,
  //                                                     @Nullable final Map<String, String> buildConfEnvironment,
  //                                                     @NotNull final String scriptSource,
  //                                                     @NotNull final String... scriptArgs)
  //  throws RakeTasksBuildService.MyBuildFailureException;

  @NotNull
  RunnerUtil.Output getGemPathsFetchLog();

  @NotNull
  RunnerUtil.Output getLoadPathsFetchLog();

  @NotNull
  RubyScriptRunner getScriptRunner();
}
