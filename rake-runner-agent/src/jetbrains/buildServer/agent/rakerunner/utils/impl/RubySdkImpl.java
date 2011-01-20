/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner.utils.impl;

import jetbrains.buildServer.agent.rakerunner.RubyLightweightSdk;
import jetbrains.buildServer.agent.rakerunner.RubySdk;
import jetbrains.buildServer.agent.rakerunner.utils.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class RubySdkImpl extends RubyLightweightSdkImpl implements RubySdk {

  private String[] myGemPaths;
  private boolean myIsRuby19;
  private boolean myIsJRuby;
  private RubyScriptRunner.Output myGemPathsLog;
  private RubyScriptRunner.Output myLoadPathsLog;
  private String[] myLoadPaths;

  public RubySdkImpl(@NotNull final RubyLightweightSdk sdk) {
    super(sdk.getInterpreterPath(),
          sdk.isRvmSdk(),
          sdk.isSystemRvm(),
          sdk.getRvmGemsetName());
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
    return myGemPathsLog;
  }

  @NotNull
  public RubyScriptRunner.Output getLoadPathsFetchLog() {
    return myLoadPathsLog;
  }

  @NotNull
  public String[] getLoadPath() {
    return myLoadPaths;
  }

  public void setIsRuby19(final boolean isRuby19) {
    myIsRuby19 = isRuby19;
  }

  public void setIsJRuby(final boolean isJRuby) {
    myIsJRuby = isJRuby;
  }

  public void setGemPathsLog(final RubyScriptRunner.Output gemPathsLog) {
    myGemPathsLog = gemPathsLog;
    myGemPaths = TextUtil.splitByLines(gemPathsLog.getStdout());

  }

  public void setLoadPathsLog(final RubyScriptRunner.Output loadPathsLog) {
    myLoadPathsLog = loadPathsLog;
    myLoadPaths = TextUtil.splitByLines(loadPathsLog.getStdout());
  }
}
