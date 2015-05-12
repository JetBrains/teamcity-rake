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

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.ModifiableRunnerContext;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import static jetbrains.buildServer.rakerunner.RakeRunnerConstants.TEST_UNIT_GEM_VERSION_PROPERTY;

/**
 * @author Roman.Chernyatchik
 */
public class TestUnitUtil {
  @NonNls
  public static final String AUTORUNNER_SCRIPT_PATH = "test/unit/autorunner.rb";
  @NonNls
  public static final String TESTRUNNERMEDIATOR_SCRIPT_PATH = "test/unit/ui/testrunnermediator.rb";
  @NonNls
  public static final String TEST_UNIT_GEM_NAME = "test-unit";
  @NonNls
  public static final String MINITEST_RUNNER_UNIT_SCRIPT_PATH = "minitest/unit.rb";

  @NotNull
  public static String getSDKTestUnitAutoRunnerScriptPath(@NotNull final RubySdk sdk,
                                                          @NotNull final ModifiableRunnerContext context)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    return findTestUnitScript(sdk, AUTORUNNER_SCRIPT_PATH, context);
  }

  @NotNull
  public static String getSDKTestUnitTestRunnerMediatorScriptPath(@NotNull final RubySdk sdk,
                                                                  @NotNull final ModifiableRunnerContext context)
    throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    return findTestUnitScript(sdk, TESTRUNNERMEDIATOR_SCRIPT_PATH, context);
  }

  @Nullable
  public static String getRuby19SDKMiniTestRunnerScriptPath(@NotNull final RubySdk sdk) {
    return findInSdkRoots(sdk, MINITEST_RUNNER_UNIT_SCRIPT_PATH);

  }


  /**
   * Finds script with given relative path in test-unit gem or Test::Unit framework bundled in SDK
   *
   * @param scriptPath      Path of given script
   * @param buildParameters Build params
   * @return Full path of given script
   * @throws jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService.MyBuildFailureException
   *          If script will not be found
   * @throws jetbrains.buildServer.RunBuildException
   *          Other error
   */
  @NotNull
  public static String findTestUnitScript(@NotNull final RubySdk sdk,
                                          @NotNull final String scriptPath,
                                          @NotNull final ModifiableRunnerContext context)
  throws RakeTasksBuildService.MyBuildFailureException, RunBuildException {

    // At first let's try to find script in "test-unit" gem
    // then in sdk load path

    final String forcedTestUnitGemVersion = RubySDKUtil.getForcedGemVersion(TEST_UNIT_GEM_VERSION_PROPERTY, context.getBuildParameters());

    // if option is "built-in" let's use built-in Test::Unit in Ruby 1.8.x sdk
    // else use custom gem version
    final boolean forceUseBuiltInTestUnit = RakeRunnerConstants.TEST_UNIT_USE_BUILTIN_VERSION_PARAM.equals(forcedTestUnitGemVersion);
    if (!forceUseBuiltInTestUnit) {

      // use bundler gems root if it is defined! (i.e. we use bundle exec emulation with custom gem paths)
      final String bundlerGemRoot = BundlerUtil.determineGemsRootsAccordingToBundlerSettings(sdk, context);
      final String[] gemPaths = bundlerGemRoot == null ? sdk.getGemPaths() : new String[]{bundlerGemRoot};

      // If user overrides bundler.path sys var or uses project custom bundle..
      // we need to look for test-unit gems in "frozen" paths
      final Pair<String, String> gemInfo;
      if (forcedTestUnitGemVersion != null) {
        gemInfo = RubySDKUtil.findGemRootFolderAndVersion(TEST_UNIT_GEM_NAME,
                                                          gemPaths,
                                                          forcedTestUnitGemVersion);
      } else {
        List<Pair<String, String>> gems = RubySDKUtil.findGemsByName(TEST_UNIT_GEM_NAME, gemPaths);
        final String version = sdk.getVersion();
        final String gv = version != null ? version + ".0" : "2.0.0.0";
        gems = CollectionsUtil.filterCollection(gems, new Filter<Pair<String, String>>() {
          public boolean accept(@NotNull final Pair<String, String> data) {
            // Ruby-2.0.0 and newer with test-unit compatibility layer using minitest
            return !data.second.equals(gv);
          }
        });
        gemInfo = gems.isEmpty() ? null : Collections.max(gems, new RubySDKUtil.GemInfoPairComparator());
      }


      if (gemInfo != null) {
        final String path = gemInfo.first;
        final String version = gemInfo.second;
        final String fullScriptPath = path + File.separatorChar + "lib" + File.separatorChar + scriptPath;
        if (FileUtil2.checkIfExists(fullScriptPath)) {
          return fullScriptPath;
        } else {

          // Error: Script wasn't found in test-unit gem
          final String msg = "Rake runner isn't compatible with your'" + TEST_UNIT_GEM_NAME + "-" + version
                             + "'(" + path + ") gem. Please submit a feature request.";
          throw new RakeTasksBuildService.MyBuildFailureException(msg);
        }

      } else {
        // test-unit gem not found
        if (forcedTestUnitGemVersion != null) {
          // not "built-in", but something specified
          final String msg = "test-unit gem with version '"
                             + forcedTestUnitGemVersion
                             + "' wasn't found in Gem paths of Ruby SDK with interpreter: '"
                             + sdk.getName()
                             + "'.\n"
                             + "Gem paths:\n"
                             + (bundlerGemRoot == null ? sdk.getGemPathsFetchLog().getStdout() : bundlerGemRoot);
          throw new RakeTasksBuildService.MyBuildFailureException(msg);
        }
      }
    }

    // find test-unit in load path
    final String fullScriptPath = findInSdkRoots(sdk, scriptPath);
    if (fullScriptPath != null) {
      return fullScriptPath;
    }

    // If stderr isn't empty / JAVA_HOME error
    final ExecResult gemPathsLog = sdk.getGemPathsFetchLog();
    RubySDKUtil.failIfWithErrors(gemPathsLog);
    final ExecResult loadPathsLog = sdk.getLoadPathsFetchLog();
    RubySDKUtil.failIfWithErrors(loadPathsLog);


    // General error message
    final StringBuilder msg = new StringBuilder();
    if (forceUseBuiltInTestUnit) {
      msg.append("You asked TC to use built-in Test::Unit test framework, but file '");
    } else {
      msg.append("File '");
    }
    msg.append(scriptPath).append("' wasn't found in Gem paths and in $LOAD_PATH of " +
                                  "Ruby SDK with interpreter: '").append(sdk.getName()).append("'.\n");
    if (sdk.isRuby19()) {
      msg.append("Rake runner detected that your are using Ruby 1.9. " +
                 "So please install 'test-unit' gem because simplified Test::Unit framework, " +
                 "which is bundled in Ruby 1.9, doesn't support pluggable test reporters.\n");
    }
    msg.append("\nGem paths:\n").append(gemPathsLog.getStdout()).append("\n\n");
    msg.append("$LOAD_PATH:\n").append(loadPathsLog.getStdout());
    throw new RakeTasksBuildService.MyBuildFailureException(msg.toString());
  }

  @Nullable
  private static String findInSdkRoots(@NotNull final RubySdk sdk,
                                       @NotNull final String relativeScriptPath) {
    final String[] loadPaths = sdk.getLoadPath();
    for (String path : loadPaths) {
      final String fullScriptPath = toSystemIndependentName(path + File.separatorChar + relativeScriptPath);
      if (FileUtil2.checkIfExists(fullScriptPath)) {
        return fullScriptPath;
      }
    }
    return null;
  }
}
