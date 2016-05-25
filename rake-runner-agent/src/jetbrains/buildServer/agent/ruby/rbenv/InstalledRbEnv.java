/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby.rbenv;

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.Collection;

import java.util.List;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.RubyVersionManager;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.impl.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class InstalledRbEnv extends RubyVersionManager {
  public static final String NAME = "rbenv";

  @NotNull
  private final File myHome;
  @NotNull
  private final Type myType;
  private final Lazy<File> myRoot = new Lazy<File>() {
    @Nullable
    @Override
    protected File createValue() {
      final String root = executeCommandLine(getExecutablePath(), "root");
      final List<String> split = StringUtil.split(root, true, '\n', '\r');
      final File file = new File(split.iterator().next());
      assert file.exists();
      return file;
    }
  };

  @NotNull
  public Type getType() {
    return myType;
  }

  public InstalledRbEnv(final @NotNull String path, final @NotNull Type type) {
    super(NAME);
    myHome = new File(path);
    myType = type;
  }

  @NotNull
  @Override
  public File getHome() {
    return myHome;
  }

  @Nullable
  @Override
  public File getRubiesFolder() {
    final File versions = new File(getRoot(), "versions");
    return versions.exists() && versions.isDirectory() ? versions : null;
  }

  @Override
  public boolean isSupportedByOs() {
    return SystemInfo.isUnix;
  }

  public void patchEnvironment(RbEnvRubySdk sdk, EnvironmentPatchableMap env) {
    env.put(Constants.RBENV_VERSION_ENV_VARIABLE, sdk.getName());
  }

  public File getRoot() {
    return myRoot.getValue();
  }

  public enum Type {
    Local,
    Global,
    Special
  }

  @NotNull
  public Collection<String> getInstalledVersions() {
    final String stdout = executeCommandLine(getExecutablePath(), "versions", "--bare");
    return StringUtil.split(stdout, true, '\n');
  }

  public boolean isVersionInstalled(@NotNull final String version) {
    return getInstalledVersions().contains(version);
  }

  @NotNull
  public File getInterpreterHome(@NotNull final String version) {
    return new File(getRubiesFolder(), version);
  }

  @NotNull
  public String getExecutablePath() {
    return new File(myHome, "bin/rbenv").getAbsolutePath();
  }

  @NotNull
  public static String executeCommandLine(@NotNull final String... query) {
    final ExecResult output = RunnerUtil.run(null, null, query);
    return output.getStdout();
  }

}
