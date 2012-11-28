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

package jetbrains.buildServer.agent.rakerunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.rakerunner.scripting.ShShellScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.impl.RubySdkImpl;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import jetbrains.buildServer.agent.ruby.rbenv.RbEnvPathsSettings;
import jetbrains.buildServer.agent.ruby.rbenv.RbEnvRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRCBasedRubySdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRubySdkImpl;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import static jetbrains.buildServer.agent.rakerunner.utils.FileUtil2.getCanonicalPath2;
import static jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil.findSystemInterpreterExecutable;
import static jetbrains.buildServer.agent.ruby.rbenv.Constants.RBENV_VERSION_ENV_VARIABLE;

/**
 * @author Vladislav.Rassokhin
 */
public enum SharedParamsType {
  NOT_SETTED("notsetted") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      return DEFAULT.createSdk(context, sharedParams);
    }
  },
  DEFAULT("default") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      return new RubySdkImpl(findSystemInterpreterExecutable(context.getBuildParameters().getEnvironmentVariables()), true);
    }
  },
  INTERPRETER_PATH("intpath") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String path = StringUtil.emptyIfNull(sharedParams.getInterpreterPath());
      InternalRubySdkUtil.checkInterpreterPathValid(path);
      return new RubySdkImpl(new File(path), false);
    }
  },
  RVM("rvmsdk") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String sdkName = StringUtil.emptyIfNull(sharedParams.getRVMSdkName());

      // at first lets check that it isn't "system" interpreter
      if (RVMSupportUtil.isSystemRuby(sdkName)) {
        final EnvironmentPatchableMap env = new EnvironmentPatchableMap(context.getBuildParameters().getEnvironmentVariables());
        final Map<String, String> patched = RVMSupportUtil.patchEnvForRVMIfNecessary2(sdkName, env);
        final RubySdk sdk = new RVMRubySdkImpl(findSystemInterpreterExecutable(patched));
        sdk.setup(patched);
        return sdk;
      }

      // build  dist/gemsets table, match ref with dist. name
      final String gemset = sharedParams.getRVMGemsetName();
      final String suitableSdkName = RVMSupportUtil.determineSuitableRVMSdkDist(sdkName, gemset, sharedParams.isRVMGemsetCreate());

      if (suitableSdkName != null) {
        if (sharedParams.isRVMGemsetCreate() && !StringUtil.isEmptyOrSpaces(gemset)) {
          List<String> gemsets = RVMSupportUtil.getInterpreterDistName2GemSetsTable().getGemsets(suitableSdkName);
          if (gemset != null && !gemsets.contains(gemset)) {
            // Creating gemset
            final ShellScriptRunner scriptRunner = new ShShellScriptRunner();
            RunnerUtil.Output output = scriptRunner.run(". $rvm_path/scripts/rvm && rvm use --create " + suitableSdkName + "@" + gemset,
                                                        context.getWorkingDirectory().getAbsolutePath(),
                                                        context.getBuildParameters().getEnvironmentVariables());
            if (!StringUtil.isEmptyOrSpaces(output.getStderr())) {
              throw new RakeTasksBuildService.MyBuildFailureException(
                "Failed to create gemset '" + gemset + "':" + output.getStderr() + "\n" + output.getStdout());
            }
          }
        }
        final File home = RVMPathsSettings.getRVMNullSafe().getHomeForVersionName(suitableSdkName);
        if (home == null) {
          throw new RakeTasksBuildService.MyBuildFailureException(
            String.format("Cannot find home path for RVM SDK with name %s", suitableSdkName));
        }
        return new RVMRubySdkImpl(home, suitableSdkName, gemset);
      }
      final String msg = "Gemset '" + gemset + "' isn't defined for Ruby interpreter '" + sdkName
                         + "' or the interpreter doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    }
  },
  RVMRC("rvmrc") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String rvmrc = StringUtil.emptyIfNull(sharedParams.getRVMRCPath());
      final String checkoutDir = getCanonicalPath2(context.getBuild().getCheckoutDirectory());
      final File file = new File(checkoutDir, StringUtil.isEmptyOrSpaces(rvmrc) ? ".rvmrc" : rvmrc);

      if (!file.exists() || !file.isFile()) {
        throw new RakeTasksBuildService.MyBuildFailureException(
          "RVMRC support: file not found. Specified path: \"" + rvmrc + "\". Resolved path: \"" + file.getAbsolutePath() + "\"",
          new FileNotFoundException(rvmrc), false);
      }

      // Create SDK using known .rvmrc file
      return RVMRCBasedRubySdkImpl
        .getOrCreate(file.getParentFile().getAbsolutePath(), context.getBuildParameters().getEnvironmentVariables());
    }
  },
  RBENV("rbenv") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final InstalledRbEnv rbEnv = RbEnvPathsSettings.getInstance().getRbEnv();
      if (rbEnv == null) {
        throw new RakeTasksBuildService.MyBuildFailureException("Cannot find rbenv. Please check that it installed on agent", false);
      }

      final String version = StringUtil.emptyIfNull(sharedParams.getRbEnvVersion());

      if ("system".equals(version)) {
        final EnvironmentPatchableMap env = new EnvironmentPatchableMap(context.getBuildParameters().getEnvironmentVariables());
        env.put(RBENV_VERSION_ENV_VARIABLE, version);
        final File executable = findSystemInterpreterExecutable(env);
        return new RubySdkImpl(executable, true);
      } else {
        if (!rbEnv.isVersionInstalled(version)) {
          throw new RakeTasksBuildService.MyBuildFailureException(
            "Specified Ruby interpreter version '" + version + "' isn't installed in rbenv");
        }
        return new RbEnvRubySdk(rbEnv.getInterpreterHome(version), version, rbEnv);
      }
    }
  },
  RBENV_FILE("rbenv_file") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final InstalledRbEnv rbEnv = RbEnvPathsSettings.getInstance().getRbEnv();
      if (rbEnv == null) {
        throw new RakeTasksBuildService.MyBuildFailureException("Cannot find rbenv. Please check that it installed on agent", false);
      }

      final String filePath = StringUtil.emptyIfNull(sharedParams.getRbEnvVersionFile());
      final String checkoutDir = getCanonicalPath2(context.getBuild().getCheckoutDirectory());
      final File file = new File(checkoutDir, StringUtil.isEmptyOrSpaces(filePath) ? ".rbenv-version" : filePath);

      final String version;
      try {
        final List<String> strings = FileUtil.readFile(file);
        if (strings.isEmpty()) {
          throw new RakeTasksBuildService.MyBuildFailureException(
            "Rbenv support: local version file is empty. Specified path: \"" + filePath + "\". Resolved path: \"" +
            file.getAbsolutePath() + "\"", false);
        }
        version = StringUtil.emptyIfNull(StringUtil.trim(strings.iterator().next()));
      } catch (IOException e) {
        throw new RakeTasksBuildService.MyBuildFailureException(
          "Rbenv support: local version file file not found. Specified path: \"" + filePath + "\". Resolved path: \"" +
          file.getAbsolutePath() + "\"", e, false);
      }

      if ("system".equals(version)) {
        final EnvironmentPatchableMap env = new EnvironmentPatchableMap(context.getBuildParameters().getEnvironmentVariables());
        env.put(RBENV_VERSION_ENV_VARIABLE, version);
        final File executable = findSystemInterpreterExecutable(env);
        return new RubySdkImpl(executable, true);
      } else {
        if (!rbEnv.isVersionInstalled(version)) {
          throw new RakeTasksBuildService.MyBuildFailureException(
            "Specified Ruby interpreter version '" + version + "' isn't installed in rbenv");
        }
        return new RbEnvRubySdk(rbEnv.getInterpreterHome(version), version, rbEnv);
      }
    }
  };

  private final String myValue;

  SharedParamsType(@NotNull final String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

  @NotNull
  public abstract RubySdk createSdk(@NotNull final BuildRunnerContext context,
                                    @NotNull final SharedParams sharedParams)
    throws RakeTasksBuildService.MyBuildFailureException;
}
