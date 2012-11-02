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

package jetbrains.buildServer.agent.ruby.rvm;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.agent.rakerunner.utils.FileUtil2;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.RubyVersionManager;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.SharedRVMUtil;

import static org.jetbrains.plugins.ruby.rvm.SharedRVMUtil.Constants.RVM_GEMS_FOLDER_NAME;
import static org.jetbrains.plugins.ruby.rvm.SharedRVMUtil.Constants.RVM_RUBIES_FOLDER_NAME;

/**
 * @author Vladislav.Rassokhin
 */
public class InstalledRVM extends RubyVersionManager {
  public static final String NAME = "rvm";
  @NotNull
  private final String myPath;
  @NotNull
  private final Type myType;

  @NotNull
  public String getPath() {
    return myPath;
  }

  @NotNull
  public Type getType() {
    return myType;
  }

  @NotNull
  @Override
  public File getHome() {
    return new File(getPath());
  }

  @NotNull
  @Override
  public File getRubiesFolder() {
    return new File(getHome(), SharedRVMUtil.Constants.RVM_RUBIES_FOLDER_NAME);
  }

  @Nullable
  public File getHomeForVersionName(@NotNull final String name) {
    final List<File> rubies = FileUtil.getSubDirectories(getRubiesFolder());
    for (File file : rubies) {
      if (file.getName().equalsIgnoreCase(name)) {
        return file;
      }
    }
    return null;
  }

  @Override
  public boolean isSupportedByOs() {
    return SystemInfo.isUnix;
  }

  public enum Type {
    Local,
    Global,
    Special
  }

  public InstalledRVM(final @NotNull String path, final @NotNull Type type) {
    super(NAME);
    this.myPath = path;
    this.myType = type;
    determineRVMConfigData();
  }

  private void determineRVMConfigData() {
    // TODO: exec rvm for data collecting
  }

  @NotNull
  public Collection<String> getRubiesNames() {
    final List<String> rubies = new ArrayList<String>();
    String stdout = executeCommandLine(getExecutablePath(), "list", "strings");
    Collections.addAll(rubies, stdout.split("\n"));
    return rubies;
  }

  private String getExecutablePath() {
    return myPath + "/bin/rvm";
  }

  @NotNull
  public static String executeCommandLine(String... query) {
    final RunnerUtil.Output output = RunnerUtil.run(null, null, query);
    return output.getStdout();
  }

  @NotNull
  public SharedRVMUtil.RubyDistToGemsetTable getInterpreterDistName2GemSetsTable() {
    final String rubyGemsFolderPath = getGemsFolderPath();
    final String rubySdksRootPath = getSdksRootPath();

    if (!FileUtil2.checkIfDirExists(rubyGemsFolderPath) || !FileUtil2.checkIfDirExists(rubySdksRootPath)) {
      return SharedRVMUtil.RubyDistToGemsetTable.emptyTable();
    }

    final File rubyGemsFolder = new File(rubyGemsFolderPath);
    final File rubySdksRoot = new File(rubySdksRootPath);

    final HashSet<String> distSet = new HashSet<String>(Arrays.asList(rubySdksRoot.list()));
    if (distSet.isEmpty()) {
      return SharedRVMUtil.RubyDistToGemsetTable.emptyTable();
    }

    final Condition<String> isRVMDistCond = new Condition<String>() {
      public boolean value(final String distName) {
        return distSet.contains(distName);
      }
    };

    final SharedRVMUtil.RubyDistToGemsetTable rubyDist2Gemset = new SharedRVMUtil.RubyDistToGemsetTable();

    // 1. scan .rvm/gems directory and find all existing (sdk, gemset) pairs
    final File[] files = rubyGemsFolder.listFiles();
    if (files != null) {
      for (File folder : files) {
        // ignore ordinary files
        if (!folder.isDirectory()) {
          continue;
        }

        // 2. Register if folder is a valid gempath for some sdk
        SharedRVMUtil.registerGemset(folder.getName(), isRVMDistCond, rubyDist2Gemset);
      }
    }

    return rubyDist2Gemset;
  }

  private String getSdksRootPath() {
    // TODO: detect via environment (may be non standard or using mixed installation)
    return getPath() + File.separatorChar + RVM_RUBIES_FOLDER_NAME;
  }

  private String getGemsFolderPath() {
    // TODO: detect via environment (may be non standard or using mixed installation)
    return getPath() + File.separatorChar + RVM_GEMS_FOLDER_NAME;
  }
}
