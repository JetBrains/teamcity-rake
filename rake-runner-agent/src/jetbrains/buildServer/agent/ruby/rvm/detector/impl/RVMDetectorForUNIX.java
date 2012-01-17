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

package jetbrains.buildServer.agent.ruby.rvm.detector.impl;

import jetbrains.buildServer.agent.rakerunner.utils.FileUtil;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.agent.ruby.rvm.detector.RVMDetector;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.SharedRVMUtil;

import java.util.Map;

import static org.jetbrains.plugins.ruby.rvm.SharedRVMUtil.Constants.RVM_BIN_FOLDER_RELATIVE_PATH;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMDetectorForUNIX extends RVMDetector {
  public static final String[] KNOW_GLOBAL_RVM_HOME_PATHS = new String[]{"/usr/local/rvm", "/opt/local/rvm"};

  @Override
  public InstalledRVM detect(@NotNull final Map<String, String> env) {
    // 1. Read "rvm_path" env variable
    // 2. Try to find local rvm (rvm_path = $HOME/.rvm)
    // 3. Try to find global rvm (rvm_path = /usr/local/.rvm)
    // NOTE: Steps 2 and 3 via 'which rvm'
    // custom or system wide rvm path

    @Nullable final String specialPath = determinePathUsingEnvVariable(env);
    @Nullable final String globalPath = determineGlobalRvmPath();
    @Nullable final String localPath = determineLocalRvmPath();

    if (specialPath != null) {
      if (localPath != null && specialPath.equals(localPath)) {
        return new InstalledRVM(localPath, InstalledRVM.Type.Local);
      } else if (globalPath != null && specialPath.equals(globalPath)) {
        return new InstalledRVM(globalPath, InstalledRVM.Type.Global);
      } else {
        return new InstalledRVM(specialPath, InstalledRVM.Type.Special);
      }
    } else if (localPath != null) {
      return new InstalledRVM(localPath, InstalledRVM.Type.Local);
    } else if (globalPath != null) {
      return new InstalledRVM(globalPath, InstalledRVM.Type.Global);
    } else {
      // Nothing founded
      return null;
    }
  }

  @Nullable
  public static String determinePathUsingEnvVariable(@NotNull final Map<String,String> env) {
    // custom path defined by env variable
    final String customRvmPath = env.get("rvm_path");
    if (!StringUtil.isEmpty(customRvmPath)) {
      return customRvmPath;
    }

    // No special rvm was found
    return null;
  }

  @Nullable
  public static String determineGlobalRvmPath() {
    // Try to detect global rvm on dist
    // Known paths: /usr/local/rvm, /opt/local/rvm
    for (String knowRvmHomePath : KNOW_GLOBAL_RVM_HOME_PATHS) {
      final String rvmExecPath = knowRvmHomePath + RVM_BIN_FOLDER_RELATIVE_PATH + "/rvm";

      try {
        if (FileUtil.checkIfExists(rvmExecPath)) {
          // rvm installation detected!
          // Checking we have permissions
          if (InstalledRVM.executeCommandLine(rvmExecPath, "-v").contains("rvm")) {
            return knowRvmHomePath;
          }
        }
      } catch (Exception ignored) {
      }
      // continue search..
    }

    // No global rvm was found
    return null;
  }

  @Nullable
  public static String determineLocalRvmPath() {
    final String home = OSUtil.getUserHomeFolder();
    // Cannot detect rvm if $HOME directory unknowed.
    if (home == null) {
      return null;
    }

    final String path = home + "/" + SharedRVMUtil.Constants.LOCAL_RVM_HOME_FOLDER_NAME;
    final String rvmExecPath = path + SharedRVMUtil.Constants.RVM_BIN_FOLDER_RELATIVE_PATH + "/rvm";
    if (FileUtil.checkIfExists(rvmExecPath)) {
      return path;
    }

    // No local rvm was found
    return null;
  }
}
