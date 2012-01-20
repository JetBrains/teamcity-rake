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

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.agent.rakerunner.utils.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubyLightweightSdk;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubySdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.SharedRVMUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRubySdkImpl extends RVMRubyLightweightSdkImpl implements RVMRubySdk {

  private boolean myIsRuby19;
  final private boolean myIsJRuby;
  final private String[] myGemPaths = new String[2];
  private RubyScriptRunner.Output myLoadPathsLog;
  private String[] myLoadPaths;
  final private RubyScriptRunner.Output myGemPathsFetchLog;

  public RVMRubySdkImpl(@NotNull RVMRubyLightweightSdk sdk) {
    super(sdk.getInterpreterPath(), sdk.getName(), sdk.isSystem(), sdk.getGemsetName());

    // Check is JRuby
    myIsJRuby = getName().startsWith("jruby");

    // get Gem Paths
    final Pair<String, String> gemPaths = SharedRVMUtil.getMainAndGlobalGemPaths(getInterpreterPath(), getGemsetName());
    if (gemPaths == null) {
      myGemPathsFetchLog = new RubyScriptRunner.Output("", "Cannot determine RVM ruby gempaths for sdk '" + getPresentableName() + "'");
    } else {
      myGemPaths[0] = gemPaths.first;
      myGemPaths[1] = gemPaths.second;
      StringBuilder sb = new StringBuilder();
      sb.append(myGemPaths[0]).append('\n');
      sb.append(myGemPaths[1]).append('\n');
      myGemPathsFetchLog = new RubyScriptRunner.Output(sb.toString(), "");
    }
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
  public RubyScriptRunner.Output getGemPathsFetchLog() {
    return myGemPathsFetchLog;
  }

  @NotNull
  public RubyScriptRunner.Output getLoadPathsFetchLog() {
    return myLoadPathsLog;
  }

  @NotNull
  public String[] getLoadPath() {
    return myLoadPaths;
  }

  public void setIsRuby19(boolean isRuby19) {
    myIsRuby19 = isRuby19;
  }

  public void setLoadPathsLog(@NotNull RubyScriptRunner.Output loadPathsLog) {
    myLoadPathsLog = loadPathsLog;
    myLoadPaths = TextUtil.splitByLines(loadPathsLog.getStdout());
  }
}
