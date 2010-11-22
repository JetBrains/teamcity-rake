/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.rvm;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.NullableFunction;
import java.io.File;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.rakerunner.RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT;

/**
 * @author Roman.Chernyatchik
 */
public class RVMPathsSettings extends SharedRVMPathsSettings {
  private static RVMPathsSettings myInstance;
  private String myRVMHomePath;

  private RVMPathsSettings() {
  }

  public static SharedRVMPathsSettings getInstance() {
    return getInstanceEx();
  }

  public static RVMPathsSettings getInstanceEx() {
    if (myInstance == null) {
      myInstance = new RVMPathsSettings();
    }
    return myInstance;
  }

  public void initialize(@NotNull final Map<String, String> buildParameters)
    throws RunBuildException, RakeTasksBuildService.MyBuildFailureException {

    myRVMHomePath = null;

    if (SystemInfo.isWindows) {
      // N/A
      throw new UnsupportedOperationException("This method is only for testing purposes");
    }

    // custom or system wide rvm path
    final String rvmPath = determineNonLocalRvmPath(new NullableFunction<String, String>() {
      public String fun(final String envVariableName) {
        return buildParameters.get(Constants.ENV_PREFIX + envVariableName);
      }
    });
    if (rvmPath != null) {
      final File rvmHome = new File(rvmPath);
      if (!rvmHome.exists()) {
        throw new RakeTasksBuildService.MyBuildFailureException("Cannot find rvm home directory: " + rvmPath,
                                                                RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
      }
      try {
        myRVMHomePath = rvmHome.getCanonicalPath();
      } catch (Exception e) {
        throw new RunBuildException(e.getMessage(), e);
      }
      return;
    }

    // Local RVM installation
    final String homeFolder = OSUtil.getUserHomeFolder();
    if (homeFolder == null) {
      throw new RakeTasksBuildService.MyBuildFailureException("Cannot find user home directory: " + System.getProperty("user.home"),
                                                              RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
    }
    final File localRvmHomeFolder = new File(homeFolder + File.separatorChar + SharedRVMUtil.Constants.LOCAL_RVM_HOME_FOLDER_NAME);
    if (localRvmHomeFolder.exists()) {
      try {
        myRVMHomePath = localRvmHomeFolder.getCanonicalPath();
      } catch (Exception e) {
        throw new RunBuildException(e.getMessage(), e);
      }
    }
  }

  @Override
  @Nullable
  public String getRvmHomePath() {
    return myRVMHomePath;
  }
}
