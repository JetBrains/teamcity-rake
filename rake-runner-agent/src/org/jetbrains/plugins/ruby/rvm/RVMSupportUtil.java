/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.rakerunner.utils.EnvUtil;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.SdkUtil;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Roman.Chernyatchik
 */
public class RVMSupportUtil {
  public static final String RVM_SYSTEM_INTERPRETER = "system";

  @NotNull
  public static SharedRVMUtil.RubyDistToGemsetTable getInterpreterDistName2GemSetsTable() {
    final InstalledRVM rvm = RVMPathsSettings.getInstance().getRVM();
    if (rvm != null) {
      return rvm.getInterpreterDistName2GemSetsTable();
    }
    return SharedRVMUtil.RubyDistToGemsetTable.emptyTable();
  }

  /**
   * Returns suitable pair (interpreter,gemset)
   * OR (interpreter,null) if (rvmGemset is null) or (no such gemset)
   * OR (null,null) otherwise
   */
  @NotNull
  public static Pair<String, String> determineSuitableRVMSdkDist(@NotNull final String interpreter,
                                                                 @Nullable final String gemset,
                                                                 final boolean allowGemsetNotExists) {
    final SharedRVMUtil.RubyDistToGemsetTable table = getInterpreterDistName2GemSetsTable();

    return SharedRVMUtil.determineSuitableRVMSdkDist(interpreter, gemset, table);
  }

  public static void patchEnvForRVMIfNecessary(@NotNull final RubySdk sdk,
                                               @NotNull final EnvironmentPatchableMap env) {
    if (SdkUtil.isRvmSdk(sdk)) {
      final EnvironmentPatchableMap old = new EnvironmentPatchableMap(env);
      env.clear();
      env.putAll(patchEnvForRVMIfNecessary2(sdk.getName(), old));
    }
  }

  public static Map<String, String> patchEnvForRVMIfNecessary2(@NotNull final String rvmRubyString,
                                                               @NotNull final EnvironmentPatchableMap env) {

    final InstalledRVM rvm = RVMPathsSettings.getRVMNullSafe();

    final Map<String, String> defaultEnvs = getDefaultEnvVarsForRvmEnvPatcher();
    final List<String> restricted = new ArrayList<String>(7);
    for (String res : SharedRVMUtil.Constants.SYSTEM_RVM_ENVVARS_TO_RESET) {
      if (!SharedRVMUtil.canOverride(res, env, defaultEnvs)) {
        restricted.add(res);
      }
    }
    final ExecResult env1 = RunnerUtil.run(null, env, rvm.getPath() + "/bin/rvm-shell", rvmRubyString, "-c", "env");
    //noinspection ThrowableResultOfMethodCallIgnored
    if (env1.getExitCode() != 0 || env1.getException() != null) {
      throw new RuntimeException("Cannot fetch sdk environment: rvm-shell failed with output" + env1.getStderr());
    }
    final Map<String, String> modified = EnvUtil.parse(env1.getStdout());
    return EnvUtil.mergeIntoNewEnv(modified, env, restricted);
  }

  @NotNull
  private static Map<String, String> getDefaultEnvVarsForRvmEnvPatcher() {
    return System.getenv();
  }

  public static void inspectCurrentEnvironment(@NotNull final EnvironmentPatchableMap envParams,
                                               @NotNull final RubySdk sdk,
                                               @NotNull final BuildProgressLogger logger) {
    // Diagnostic check:

    if (SdkUtil.isRvmSdk(sdk)) {
      // rvm sdk

      // RVM support can override only "default" process values. Build env vars and agent env. vars
      // wont be overriden. Thus lets check which potentially dangerous environment variables
      // won't be overriden and inform user about them

      // do check
      final Map<String, String> defaultEnvs = getDefaultEnvVarsForRvmEnvPatcher();
      for (String envVarName : SharedRVMUtil.Constants.SYSTEM_RVM_ENVVARS_TO_RESET) {
        if (!SharedRVMUtil.canOverride(envVarName, envParams, defaultEnvs)) {
          final String value = envParams.get(envVarName);
          // info msg - most likely user understand what he is doing.
          logger.warning("Environment variable '" + envVarName + "' has predefined value '" + value +
                         "'. It may affect runtime build behaviour because TeamCity RVM support won't override it.");
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
                         "'. It may affect runtime build behaviour because TeamCity Ruby support won't override it.");
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
