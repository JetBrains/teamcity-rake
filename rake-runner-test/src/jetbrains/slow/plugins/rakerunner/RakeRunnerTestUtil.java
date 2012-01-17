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

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Roman.Chernyatchik
 */
public class RakeRunnerTestUtil {
  public static final String TESTDATA_PATH = "svnrepo/rake-runner/rake-runner-test/testData/";
  public static final String INTERPRETER_PATH_PROPERTY = "rake-runner.ruby.interpreter.path";
  public static final String RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY = "rake-runner.testing.ruby.version";
  public static final String DEFAULT_GEMSET_NAME = "all-trunk";
  public static final String GEMSET_PREFIX = "";

  @NotNull
  static File getTestDataItemPath(@NotNull final String fileOrFolderRelativePath) {
    return new File(TESTDATA_PATH + fileOrFolderRelativePath);
  }

  static public void setInterpreterPath(@NotNull final BuildTypeSettings bt) {
    String interpreterPath = System.getProperty(INTERPRETER_PATH_PROPERTY);
    if (!StringUtil.isEmpty(interpreterPath)) {
      bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH, interpreterPath));
      bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
          RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH.getModeValueString()));
    }
  }

  static public void setRVMConfiguration(@NotNull final BuildTypeSettings bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
        RakeRunnerUtils.RubyConfigMode.RVM.getModeValueString()));
    useRVMRubySDK(System.getProperty(RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY), bt);
    useRVMGemSet(DEFAULT_GEMSET_NAME, bt);
  }

  static public void useRVMRubySDK(@NotNull final String sdkname, @NotNull final BuildTypeSettings bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, sdkname));
  }

  static public void useRVMGemSet(@NotNull final String gemset, @NotNull final BuildTypeSettings bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME, GEMSET_PREFIX + gemset));
  }
}
