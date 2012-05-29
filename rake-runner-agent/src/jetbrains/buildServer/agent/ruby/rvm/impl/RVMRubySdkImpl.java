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
import java.io.File;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.RvmShellRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellBasedRubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.impl.RubySdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubySdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;
import org.jetbrains.plugins.ruby.rvm.SharedRVMUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRubySdkImpl extends RubySdkImpl implements RVMRubySdk {

  @Nullable
  private final String myGemsetName;
  @NotNull
  private final String myName;
  @NotNull
  private final ShellBasedRubyScriptRunner myRubyScriptRunner;

  public RVMRubySdkImpl(@NotNull final String interpreterPath,
                        @NotNull final String name,
                        final boolean isSystem,
                        @Nullable final String gemsetName) {
    super(interpreterPath, isSystem);
    myGemsetName = gemsetName;
    myName = name;
    myRubyScriptRunner = new ShellBasedRubyScriptRunner(new MyRvmShellRunner());

    // Autofill some parameters

    // get Gem Paths
    final Pair<String, String> gemPaths = SharedRVMUtil.getMainAndGlobalGemPaths(getInterpreterPath(), getGemsetName());
    if (gemPaths == null) {
      setGemPathsLog(new RunnerUtil.Output("", "Cannot determine RVM ruby gempaths for sdk '" + getPresentableName() + "'"));
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(gemPaths.first).append('\n');
      sb.append(gemPaths.second).append('\n');
      setGemPathsLog(new RunnerUtil.Output(sb.toString(), ""));
    }
  }

  @Override
  public void setup(@NotNull final Map<String, String> env) {
    if (isSetupCompleted()) {
      return;
    }
    if (!this.isSystem()) {
      // language level
      setIsRuby19(InternalRubySdkUtil.isRuby19Interpreter(this, env));

      // language level
      setIsJRuby(InternalRubySdkUtil.isJRubyInterpreter(this, env));

      // load path
      setLoadPathsLog(InternalRubySdkUtil.getLoadPaths(this, env));

      // Other already initialized
      setIsSetupCompleted(true);
    } else {
      super.setup(env);
    }
  }

  @Nullable
  public String getGemsetName() {
    return myGemsetName;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  public final boolean isRvmSdk() {
    return true;
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return myGemsetName == null
           ? getName()
           : getName() + RVMSupportUtil.getGemsetSeparator() + myGemsetName;
  }

  @NotNull
  @Override
  public RubyScriptRunner getScriptRunner() {
    return myRubyScriptRunner;
  }

  private class MyRvmShellRunner extends RvmShellRunner {
    public MyRvmShellRunner() {
      super(RVMPathsSettings.getRVMNullSafe());
    }

    @Override
    protected String[] createProcessArguments(final String rvmShellEx, final String workingDirectory, final File scriptFile) {
      return new String[]{rvmShellEx, getPresentableName(), scriptFile.getAbsolutePath()};
    }
  }
}
