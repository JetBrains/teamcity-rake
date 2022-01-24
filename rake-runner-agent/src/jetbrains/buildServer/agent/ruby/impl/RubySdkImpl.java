/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.ProcessBasedRubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.RubyVersionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public class RubySdkImpl implements RubySdk {

  @Nullable
  private final File myHome;
  @NotNull
  private final File myExecutablePath;
  private final boolean myIsSystem;
  @NotNull
  private final String myRubyName;

  private String myVersion = null;
  private boolean myIsJRuby;
  private String[] myGemPaths;
  private String[] myLoadPaths;
  private ExecResult myGemPathsLog;
  private ExecResult myLoadPathsLog;
  private boolean myIsSetupCompleted = false;

  public RubySdkImpl(@SuppressWarnings("NullableProblems") @NotNull final File home,
                     @SuppressWarnings("NullableProblems") @Nullable final File executable) {
    myHome = home;
    myIsSystem = false;
    myRubyName = defineName(myHome.getName().toLowerCase(), true);

    if (executable != null) {
      myExecutablePath = executable;
    } else {
      StringBuilder executableName = new StringBuilder(myRubyName);
      if (SystemInfo.isWindows) {
        executableName.append(".exe");
      }
      myExecutablePath = new File(myHome, "bin" + File.separator + executableName.toString());
    }
  }

  public RubySdkImpl(@NotNull final File executable, boolean isSystem) {
    myHome = null;
    myExecutablePath = executable;
    myIsSystem = isSystem;
    myRubyName = defineName(myExecutablePath.getName().toLowerCase(), false);
  }

  public interface RubyNames {
    String RUBY = "ruby";
    String MACRUBY = "macruby";
    String RBX = "rbx";
    String REE = "ree";
    String JRUBY = "jruby";
    String IRONRUBY = "ir";
    String MAGLEV = "maglev";
  }

  @NotNull
  private String defineName(@NotNull final String name, boolean isThrow) {
    if (name.startsWith(RubyNames.RUBY) || name.startsWith(RubyNames.REE) || name.startsWith(RubyNames.MACRUBY) ||
        name.startsWith(RubyNames.RBX) || name.matches("^\\d.*")) {
      return RubyNames.RUBY;
    } else if (name.startsWith(RubyNames.JRUBY)) {
      return RubyNames.JRUBY;
    } else if (name.startsWith(RubyNames.IRONRUBY)) {
      return RubyNames.IRONRUBY;
    } else if (name.startsWith(RubyNames.MAGLEV)) {
      return "maglev-ruby";
    } else if (isThrow) {
      throw new IllegalStateException(String.format("Unsupported Ruby SDK name '%s'", name));
    } else {
      return name;
    }
  }

  @NotNull
  public String[] getGemPaths() {
    return myGemPaths;
  }

  public boolean isRuby19() {
    return myVersion != null && myVersion.contains("1.9.");
  }

  public boolean isJRuby() {
    return myIsJRuby;
  }

  @NotNull
  public ExecResult getGemPathsFetchLog() {
    return myGemPathsLog;
  }

  @NotNull
  public ExecResult getLoadPathsFetchLog() {
    return myLoadPathsLog;
  }

  @NotNull
  public RubyScriptRunner getScriptRunner() {
    return new ProcessBasedRubyScriptRunner(this);
  }

  @Nullable
  public String getVersion() {
    return myVersion;
  }

  @NotNull
  public String[] getLoadPath() {
    return myLoadPaths;
  }

  public void setVersion(final String version) {
    myVersion = version;
  }

  public void setIsJRuby(final boolean isJRuby) {
    myIsJRuby = isJRuby;
  }

  public void setGemPathsLog(final ExecResult gemPathsLog) {
    myGemPathsLog = gemPathsLog;
    myGemPaths = TextUtil.splitByLines(gemPathsLog.getStdout());

  }

  public void setLoadPathsLog(final ExecResult loadPathsLog) {
    myLoadPathsLog = loadPathsLog;
    myLoadPaths = TextUtil.splitByLines(loadPathsLog.getStdout());
  }

  @Nullable
  public File getHome() {
    return myHome;
  }

  @NotNull
  public File getRubyExecutable() {
    return myExecutablePath;
  }

  public boolean isSystem() {
    return myIsSystem;
  }

  @Nullable
  public RubyVersionManager getVersionManager() {
    return null;
  }

  @Nullable
  public String getGemset() {
    return null;
  }

  @NotNull
  public String getName() {
    return myHome != null ? myHome.getName() : myExecutablePath.getAbsolutePath();
  }

  public void setup(@NotNull final Map<String, String> env) {
    if (myIsSetupCompleted) {
      return;
    }

    // ruby version
    setVersion(InternalRubySdkUtil.getRubyInterpreterVersion(this, env));

    // ruby / jruby
    setIsJRuby(InternalRubySdkUtil.isJRubyInterpreter(this, env));

    // gem paths
    setGemPathsLog(InternalRubySdkUtil.getGemPaths(this, env));

    // load path
    setLoadPathsLog(InternalRubySdkUtil.getLoadPaths(this, env));

    // Set setup completed
    myIsSetupCompleted = true;
  }
}
