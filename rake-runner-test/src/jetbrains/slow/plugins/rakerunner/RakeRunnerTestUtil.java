/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.slow.plugins.rakerunner;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.containers.HashMap;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Set;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import jetbrains.buildServer.agent.ruby.rbenv.detector.RbEnvDetectorForUNIX;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.agent.ruby.rvm.detector.impl.RVMDetectorForUNIX;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.impl.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class RakeRunnerTestUtil {
  public static final String TESTDATA_PATH = "rake-runner-test/testData/";
  public static final String INTERPRETER_PATH_PROPERTY = "rake-runner.ruby.interpreter.path";
  public static final String INTERPRETERS_STORAGE_PATH_PROPERTY = "rake-runner.ruby.interpreters.storage.path";
  public static final String RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY = "rake-runner.testing.ruby.version";
  public static final String DEFAULT_GEMSET_NAME = "all-trunk";
  public static final String GEMSET_PREFIX = "";
  private static final Lazy<InstalledRVM> RVM = new Lazy<InstalledRVM>() {
    @Nullable
    @Override
    protected InstalledRVM createValue() {
      if (!SystemInfo.isUnix) return null;
      return new RVMDetectorForUNIX().detect(System.getenv());
    }
  };
  private static final Lazy<InstalledRbEnv> RBENV = new Lazy<InstalledRbEnv>() {
    @Nullable
    @Override
    protected InstalledRbEnv createValue() {
      if (!SystemInfo.isUnix) return null;
      return new RbEnvDetectorForUNIX().detect(System.getenv());
    }
  };


  public static InstalledRVM getRvm() {
    return RVM.getValue();
  }

  public static InstalledRbEnv getRbenv() {
    return RBENV.getValue();
  }

  protected static boolean isUseRVM() {
    return RVM.getValue() != null;
  }

  protected static boolean isUseRbEnv() {
    return RBENV.getValue() != null;
  }

  public static class InterpreterNotFoundException extends Exception {
    InterpreterNotFoundException(final String message) {
      super(message);
    }
  }

  @NotNull
  public static File getTestDataItemPath(@NotNull final String fileOrFolderRelativePath) {
    if (new File("svnrepo").getAbsoluteFile().exists()) {
      // Full BuildServer tests
      return new File("svnrepo/rake-runner/" + TESTDATA_PATH + fileOrFolderRelativePath);
    }
    // In case of standalone tests run
    return new File(TESTDATA_PATH + fileOrFolderRelativePath);
  }

  static public void setInterpreterPath(@NotNull final BuildTypeEx bt) throws InterpreterNotFoundException {
    String interpreterPath = System.getProperty(INTERPRETER_PATH_PROPERTY);
    if (StringUtil.isEmpty(interpreterPath)) {
      for (String prefix : RubyVersionsDataProvider.getRubyVersionsWindowsSet()) {
        try {
          final File file = getWindowsInterpreterExecutableFile(prefix);
          interpreterPath = file.getAbsolutePath();
        } catch (InterpreterNotFoundException ignore) {
        }
      }
    }
    if (StringUtil.isEmpty(interpreterPath)) {
      interpreterPath = OSUtil.findRubyInterpreterInPATH(System.getenv());
    }
    if (StringUtil.isEmpty(interpreterPath)) {
      throw new InterpreterNotFoundException("Cannot find interpreter in PATH, by property '" + INTERPRETER_PATH_PROPERTY +
        "' and in storage '" + INTERPRETERS_STORAGE_PATH_PROPERTY + "'");
    } else {
      bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH, interpreterPath));
      bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
        RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH.getModeValueString()));
    }
  }

  static public void setInterpreterPath(@NotNull final BuildTypeEx bt, @NotNull final String prefix)
    throws InterpreterNotFoundException {
    final File interpreter = getWindowsInterpreterExecutableFile(prefix);
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH, interpreter.getAbsolutePath()));
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
        RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH.getModeValueString()));
  }

  @NotNull
  public static File getWindowsInterpreterExecutableFile(@NotNull final String prefix) throws InterpreterNotFoundException {
    String interpretersStoragePath = System.getProperty(INTERPRETERS_STORAGE_PATH_PROPERTY);
    if (StringUtil.isEmptyOrSpaces(interpretersStoragePath)) {
      throw new InterpreterNotFoundException("Cannot found interpreters storage location. Please specify \"" +
                                             INTERPRETERS_STORAGE_PATH_PROPERTY + "\" property as path to interpreters storage");
    }

    File storageDir = new File(interpretersStoragePath);
    final File[] acceptablePaths = storageDir.listFiles(new FileFilter() {
      public boolean accept(@NotNull final File pathname) {
        return pathname.isDirectory() && pathname.getName().startsWith(prefix);
      }
    });
    System.out.println("interpretersStoragePath = " + interpretersStoragePath);
    if (acceptablePaths == null || acceptablePaths.length == 0) {
      throw new InterpreterNotFoundException("Cannot found any interpreter path with prefix \"" + prefix + "\"");
    }
    if (acceptablePaths.length != 1) {
      throw new InterpreterNotFoundException(
        "There more than one interpreter path with prefix \"" + prefix + "\", founded " + acceptablePaths.length + " path");
    }
    File interpreter = new File(acceptablePaths[0], "/bin/" + (prefix.startsWith("jruby") ? "jruby.bat" : "ruby.exe"));
    if (!interpreter.exists() || !interpreter.isFile()) {
      throw new InterpreterNotFoundException("Founded path is not exist \"" + interpreter.getAbsolutePath() + "\"");
    }
    return interpreter;
  }

  static public void setRVMConfiguration(@NotNull final BuildTypeEx bt, @NotNull final String rubySdkName) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
                                           RakeRunnerUtils.RubyConfigMode.RVM.getModeValueString()));
    useRVMRubySDK(rubySdkName, bt);
    useRVMGemSet(DEFAULT_GEMSET_NAME, bt);
  }

  static public void setRVMConfiguration(@NotNull final BuildTypeEx bt) {
    setRVMConfiguration(bt, getDeafultRubySdk());
  }

  static public void setRbEnvConfiguration(@NotNull final BuildTypeEx bt, @NotNull final String rubySdkName) {
    // Ensure there is build feature
    getOrCreateRbEnvFeature(bt);

    // Build step should inherit ruby sdk from feature
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
                                           RakeRunnerUtils.RubyConfigMode.DEFAULT.getModeValueString()));

    useRbEnvRubySDK(rubySdkName, bt);
  }

  static public void setRbEnvConfiguration(@NotNull final BuildTypeEx bt) {
    setRbEnvConfiguration(bt, getDeafultRubySdk());
  }

  private static String getDeafultRubySdk() {
    String property = System.getProperty(RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY);
    if (StringUtil.isEmptyOrSpaces(property)) {
      final Set<String> set = RubyVersionsDataProvider.getRubyVersionsLinuxSet();
      if (!set.isEmpty()) {
        property = set.iterator().next();
      } else {
        throw new IllegalStateException("Required at least one interpreter");
      }
    }
    return property;
  }

  static public void useRVMRubySDK(@NotNull final String sdkname, @NotNull final BuildTypeEx bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, sdkname));
  }

  static public void useRbEnvRubySDK(@NotNull final String sdkname, @NotNull final BuildTypeEx bt) {
    final SBuildFeatureDescriptor feature = getOrCreateRbEnvFeature(bt);
    final HashMap<String, String> params = new HashMap<String, String>(feature.getParameters());
    params.put(RubyEnvConfiguratorConstants.UI_RBENV_VERSION_NAME_KEY, sdkname);
    bt.updateBuildFeature(feature.getId(), feature.getType(), params);
  }

  private static SBuildFeatureDescriptor getOrCreateRbEnvFeature(final BuildTypeEx bt) {
    final Collection<SBuildFeatureDescriptor> found = bt.getBuildFeaturesOfType(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE);
    if (!found.isEmpty()) {
      return found.iterator().next();
    }

    final HashMap<String, String> params = new HashMap<String, String>();
    params.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rbenv");
    params.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());
    return bt.addBuildFeature(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE, params);
  }

  static public void useRVMGemSet(@NotNull final String gemset, @NotNull final BuildTypeEx bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME, GEMSET_PREFIX + gemset));
  }

  static public void useBundleExec(@NotNull final BuildTypeEx bt, boolean value) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY, Boolean.toString(value)));
  }
}
