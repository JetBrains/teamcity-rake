

package jetbrains.buildServer.agent.rakerunner;

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.rakerunner.scripting.BashShellScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.FileUtil2;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
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
      if (StringUtil.isEmpty(path)) {
        return DEFAULT.createSdk(context, sharedParams);
      }
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
      final Pair<String, String> suitable = RVMSupportUtil.determineSuitableRVMSdkDist(sdkName, gemset);

      if (suitable.first == null) {
        throw new RakeTasksBuildService.MyBuildFailureException(String.format("RVM interpreter '%s' doesn't exist " +
          "or isn't a file or isn't a valid RVM interpreter name.", sdkName));
      } else {
        final File home = RVMPathsSettings.getRVMNullSafe().getHomeForVersionName(suitable.first);
        if (home == null) {
          throw new RakeTasksBuildService.MyBuildFailureException(String.format("Cannot find home path for RVM SDK with name %s", suitable.first));
        }
        if (suitable.second == null && !StringUtil.isEmptyOrSpaces(gemset)) {
          if (sharedParams.isRVMGemsetCreate()) {
            // Creating gemset
            final ShellScriptRunner scriptRunner = new BashShellScriptRunner();
            final ExecResult output = scriptRunner.run(". $rvm_path/scripts/rvm && rvm use --create " + suitable.first + "@" + gemset,
              context.getWorkingDirectory().getAbsolutePath(),
              context.getBuildParameters().getEnvironmentVariables());
            //noinspection ThrowableResultOfMethodCallIgnored
            if (output.getExitCode() != 0 || output.getException() != null) {
              throw new RakeTasksBuildService.MyBuildFailureException("Failed to create gemset '" + gemset + "':" + output);
            }
            return new RVMRubySdkImpl(home, suitable.first, gemset);
          } else {
            throw new RakeTasksBuildService.MyBuildFailureException(String.format("Gemset '%s' isn't defined for RVM interpreter '%s'. " +
              "You may enable 'Create gemset if not exist' option in Ruby Environment Configurator build feature.", gemset, sdkName));
          }
        }
        return new RVMRubySdkImpl(home, suitable.first, suitable.second);
      }
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
  RVM_RUBY_VERSION("rvm_ruby_version") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final BuildRunnerContext context,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String path = StringUtil.emptyIfNull(sharedParams.getRVMRubyVersionPath());
      final File checkoutDir = context.getBuild().getCheckoutDirectory();

      final File p;
      if (StringUtil.isEmpty(path)) {
        p = checkoutDir.getAbsoluteFile();
      } else {
        p = new File(checkoutDir, path).getAbsoluteFile();
      }

      final File version = new File(p, ".ruby-version");

      if (!version.exists() || !version.isFile()) {
        throw new RakeTasksBuildService.MyBuildFailureException(
          "RVM support: .ruby-version file not found in folder: \"" + path + "\". (Resolved folder: \"" + p.getAbsolutePath() + "\")",
          new FileNotFoundException(version.getAbsolutePath()), false);
      }

      //final File gemset = new File(p, ".ruby-gemset");

      // Create SDK
      return RVMRCBasedRubySdkImpl.getOrCreate(p.getAbsolutePath(), context.getBuildParameters().getEnvironmentVariables());
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

      final String path = StringUtil.emptyIfNull(sharedParams.getRbEnvVersionFile());
      final File cd = context.getBuild().getCheckoutDirectory();
      final File file;
      if (!StringUtil.isEmptyOrSpaces(path)) {
        final File f = new File(cd, path);
        if (f.isDirectory()) {
          file = FileUtil2.getFirstExistChild(f, ".ruby-version", ".rbenv-version");
        } else {
          file = f; // For backward compatibility (enter file name, not directory)
        }
      } else {
        file = FileUtil2.getFirstExistChild(cd, ".ruby-version", ".rbenv-version");
      }

      if (file == null) {
        throw new RakeTasksBuildService.MyBuildFailureException(
          "Rbenv support: local version file file not found. Specified path: \"" + path + "\". Resolved path: \"" +
          new File(cd, path).getAbsolutePath() + "\"", false);
      }

      final String version;
      try {
        final List<String> strings = FileUtil.readFile(file);
        if (strings.isEmpty()) {
          throw new RakeTasksBuildService.MyBuildFailureException(
            "Rbenv support: local version file is empty. Specified path: \"" + path + "\". Resolved path: \"" +
            file.getAbsolutePath() + "\"", false);
        }
        version = StringUtil.emptyIfNull(StringUtil.trim(strings.iterator().next()));
      } catch (IOException e) {
        throw new RakeTasksBuildService.MyBuildFailureException(
          "Rbenv support: cannot read local version file. Specified path: \"" + path + "\". Resolved path: \"" +
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