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

package org.jetbrains.plugins.ruby.rvm;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.rakerunner.RubyLightweightSdk;
import jetbrains.buildServer.agent.rakerunner.utils.FileUtil;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.jetbrains.plugins.ruby.rvm.SharedRVMUtil.Constants.RVM_GEMS_FOLDER_NAME;
import static org.jetbrains.plugins.ruby.rvm.SharedRVMUtil.Constants.RVM_RUBIES_FOLDER_NAME;

/**
 * @author Roman.Chernyatchik
 */
public class RVMSupportUtil {
  private static final String RVM_SYSTEM_INTERPRETER = "system";

  public static boolean isRVMInterpreter(final String executablePath) {
    return SharedRVMUtil.isRVMInterpreter(executablePath);
  }

  @NotNull
  public static SharedRVMUtil.RubyDistToGemsetTable getInterpreterDistName2GemSetsTable() {
    if (SystemInfo.isUnix) {

      final String rvmHomeDirPath = RVMPathsSettings.getInstance().getRvmHomePath();
      if (rvmHomeDirPath != null) {
        return getInterpreterDistName2GemSetsTable(rvmHomeDirPath);
      }
    }
    return SharedRVMUtil.RubyDistToGemsetTable.emptyTable();
  }

  @NotNull
  private static SharedRVMUtil.RubyDistToGemsetTable getInterpreterDistName2GemSetsTable(@NotNull final String rvmHomeDirPath) {
    final String rubyGemsFolderPath = rvmHomeDirPath + File.separatorChar + RVM_GEMS_FOLDER_NAME;
    final String rubySdksRootPath = rvmHomeDirPath + File.separatorChar + RVM_RUBIES_FOLDER_NAME;

    if (!FileUtil.checkIfDirExists(rubyGemsFolderPath) || !FileUtil.checkIfDirExists(rubySdksRootPath)) {
      return SharedRVMUtil.RubyDistToGemsetTable.emptyTable();
    }

    final File rubyGemsFolder = new File(rubyGemsFolderPath);
    final File rubySdksRoot = new File(rubySdksRootPath);

    final HashSet<String> distSet = new HashSet<String>(Arrays.asList(rubySdksRoot.list()));
    if (distSet.isEmpty()) {
      return SharedRVMUtil.RubyDistToGemsetTable.emptyTable();
    }

    final SharedRVMUtil.RubyDistToGemsetTable rubyDist2Gemset = new SharedRVMUtil.RubyDistToGemsetTable();

    // 1. scan .rvm/gems directory and find all existing (sdk, gemset) pairs
    for (File folder : rubyGemsFolder.listFiles()) {
      // ignore ordnary files
      if (!folder.isDirectory()) {
        continue;
      }

      final Condition<String> isRVMDistCond = new Condition<String>() {
        public boolean value(final String distName) {
          return distSet.contains(distName);
        }
      };

      // 2. Register if folder is a valid gempath for some sdk
      SharedRVMUtil.registerGemset(folder.getName(),
                                   isRVMDistCond,
                                   rubyDist2Gemset);
    }

    return rubyDist2Gemset;
  }

  public static boolean isGemsetExists(@NotNull final String rvmGemset, 
                                       @NotNull final String rubyInterpreterPath)
    throws RakeTasksBuildService.MyBuildFailureException {
    final Pair<String, String> gemsRootAndDistName = getNormalizedDistAndGemset(rubyInterpreterPath);

    if (gemsRootAndDistName == null) {
      return false;
    }

    final String gemsRoot = gemsRootAndDistName.first;
    final String distName = gemsRootAndDistName.second;

    return FileUtil.checkIfDirExists(gemsRoot + File.separatorChar
                                     + distName + SharedRVMUtil.getGemsetSeparator() + rvmGemset);
  }

  @Nullable
  public static String dumpAvailableGemsets(@NotNull final String rubyInterpreterPath) 
    throws RakeTasksBuildService.MyBuildFailureException {

    final SharedRVMUtil.RubyDistToGemsetTable table = getInterpreterDistName2GemSetsTable();

    final Pair<String, String> distAndGemset = getNormalizedDistAndGemset(rubyInterpreterPath);
    if (distAndGemset == null) {
      return null;
    }
    final String dist = distAndGemset.second;
    final List<String> gemsets = table.getGemsets(dist);
    if (gemsets == null) {
      return null;
    }

    // dump
    final StringBuilder buff = new StringBuilder();
    for (String gemset : gemsets) {
      if (buff.length() != 0) {
        buff.append(", ");
      }
      buff.append(gemset == null ? "[default]" : gemset);
    }
    return buff.toString();
  }

  private static Pair<String, String> getNormalizedDistAndGemset(final String rubyInterpreterPath)
    throws RakeTasksBuildService.MyBuildFailureException {
    final Pair<String, String> gemsRootAndDistName;
    try {
      gemsRootAndDistName = SharedRVMUtil.getRVMGemsRootAndDistName(rubyInterpreterPath);
    } catch (IllegalArgumentException e) {
      // dist wasn't determined
      throw new RakeTasksBuildService.MyBuildFailureException(e.getMessage());
    }
    return gemsRootAndDistName;
  }

  @NotNull
  public static String suggestInterpretatorPath(@NotNull final String distName) {
    final String rvmHomePath = RVMPathsSettings.getInstance().getRvmHomePath();
    if (rvmHomePath == null) {
      throw new IllegalArgumentException("RVM home cannot be unkown here.");
    }

    // rvm defines "ruby" symlink for all ruby interpreters
    return rvmHomePath
           + File.separator + RVM_RUBIES_FOLDER_NAME
           + File.separator + distName
           + File.separator + "bin"
           + File.separator + "ruby";
  }

  public static String determineSuitableRVMSdkDist(final String uiRubyInterpreterSetting, final String rvmGemset) {
    final SharedRVMUtil.RubyDistToGemsetTable table = getInterpreterDistName2GemSetsTable();

    return SharedRVMUtil.determineSuitableRVMSdkDist(uiRubyInterpreterSetting,
                                                     rvmGemset, table);
  }

  public static void patchEnvForRVMIfNecessary(@NotNull final RubyLightweightSdk sdk,
                                               final Map<String, String> envParams)
    throws RakeTasksBuildService.MyBuildFailureException {

    if (!sdk.isRvmSdk()) {
      // do nothing
      return;
    }

    // patch
    final LinkedHashSet<String> gemRootsPaths = sdk.isSystemRvm()
                                                ? new LinkedHashSet<String>()
                                                : SharedRVMUtil.determineGemRootsPaths(sdk.getInterpreterPath(), sdk.getRvmGemsetName(), false);

    try {
      SharedRVMUtil.patchEnvForRVM(sdk.getInterpreterPath(),
                                   sdk.getRvmGemsetName(),
                                   sdk.isSystemRvm(),
                                   gemRootsPaths,
                                   envParams,
                                   envParams, // system + buildAgent.config + build properties env. vars
                                   false,
                                   File.pathSeparatorChar,
                                   OSUtil.getPATHEnvVariableKey(),
                                   getDefaultEnvVarsForRvmEnvPatcher());  // system env. vars
    } catch (IllegalArgumentException e) {
      throw new RakeTasksBuildService.MyBuildFailureException(e.getMessage());
    }
  }

  private static Map<String, String> getDefaultEnvVarsForRvmEnvPatcher() {
    return System.getenv();
  }

  public static void inspectCurrentEnvironment(final Map<String, String> envParams,
                                               final RubyLightweightSdk sdk,
                                               @NotNull final BuildProgressLogger logger) {
    // Diagnostic check:

    if (sdk.isRvmSdk()) {
      // rvm sdk

      // RVM support can ovveride only "default" process values. Build env vars and agent env. vars
      // wont be overriden. Thus lets check which potentially dangerous environment variables
      // won't be overriden and inform user about them

      // do check
      final Map<String, String> defaultEnvs = getDefaultEnvVarsForRvmEnvPatcher();
      for (String envVarName : SharedRVMUtil.Constants.SYSTEM_RVM_ENVVARS_TO_RESET) {
        if (!SharedRVMUtil.canOverride(envVarName, envParams, defaultEnvs)) {
          final String value = envParams.get(envVarName);
          // info msg - most likely user understand what he is doing.
          logger.message("Environment variable '" + envVarName + "' has predefined value '" + value + "'. It may affect runtime build behaviour because TeamCity RVM support wont override it.");
        }
      }
    } else {
      // non-rvm sdk

      // TC patches only PATH env variable for non-rvm sdks,
      // thus following env variables are potentially dangerous.
      // (e.g. user launched TC agent from rvm-enabled console)
      final String[] variablesToCheck = new String[]{
        SharedRVMUtil.Constants.GEM_PATH,
        SharedRVMUtil.Constants.GEM_HOME,
        SharedRVMUtil.Constants.BUNDLE_PATH,
      };

      // do check
      for (String envVarName : variablesToCheck) {
        if (envParams.containsKey(envVarName)) {
          final String value = envParams.get(envVarName);
          // warning - most likely user doesn't understand what he is doing.
          logger.warning("Environment variable '" + envVarName + "' has predefined value '" + value +
                         "'. It may affect runtime build behaviour because TeamCity Ruby support wont override it.");
        }
      }
    }
  }

  public static String getGemsetSeparator() {
    return SharedRVMUtil.getGemsetSeparator();
  }

  public static boolean isSystemRuby(@Nullable final String rvmInterpreterName) {
    return RVM_SYSTEM_INTERPRETER.equals(rvmInterpreterName);
  }
}
