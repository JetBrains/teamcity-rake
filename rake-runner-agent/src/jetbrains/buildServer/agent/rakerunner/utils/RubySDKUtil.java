/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

/**
 * @author Roman.Chernyatchik
 */
public class RubySDKUtil {
  @NonNls
  public static final String AUTORUNNER_SCRIPT_PATH = "test/unit/autorunner.rb";
  @NonNls
  public static final String TESTRUNNERMEDIATOR_SCRIPT_PATH = "test/unit/ui/testrunnermediator.rb";

  @NonNls
  private static final String GET_LOAD_PATH_SCRIPT =  "puts $LOAD_PATH";
  @NonNls
  private static final String GET_GEM_PATHES_SCRIPT =  "require 'rubygems'; puts Gem.path";
  @NonNls
  private static final String GEMS_SUBDIR =  "/gems";

  @NotNull
  public static String getSDKTestUnitAutoRunnerScriptPath(@NotNull final Map<String, String> runParameters,
                                                          @NotNull final Map<String, String> buildParameters)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    return findSdkScript(runParameters, buildParameters, AUTORUNNER_SCRIPT_PATH);
  }

  @NotNull
  public static String getSDKTestUnitTestRunnerMediatorScriptPath(@NotNull final Map<String, String> runParameters,
                                                                  @NotNull final Map<String, String> buildParameters)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    return findSdkScript(runParameters, buildParameters, TESTRUNNERMEDIATOR_SCRIPT_PATH);
  }

//  public static boolean isGemInstalledInSDK(@NotNull final String gemName,
//                                            @Nullable final String gemVersion,
//                                            final boolean acceptHigherVersions,
//                                            @NotNull final Map<String, String> runParameters,
//                                            @NotNull final Map<String, String> buildParameters)
//      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {
//
//    final String scriptSource = GET_GEM_PATHES_SCRIPT;
//    final RubyScriptRunner.Output result = executeScriptFromSource(runParameters, buildParameters, scriptSource);
//    final String gemPaths[] = TextUtil.splitByLines(result.getStdout());
//    for (String gemPath : gemPaths) {
//      final String gemsRootFolderPath = toSystemIndependentName(gemPath + GEMS_SUBDIR);
//      final File gemsRootFolderFile = new File(gemsRootFolderPath);
//      try {
//        if (!gemsRootFolderFile.isDirectory()) {
//          continue;
//        }
//        final File[] files = gemsRootFolderFile.listFiles(new FilenameFilter() {
//          final String gemNamePrefix = gemName + "-";
//
//          public boolean accept(final File dir, @NotNull final String fileName) {
//            if (!dir.equals(gemsRootFolderFile)) {
//              return false;
//            }
//
//            if (!fileName.startsWith(gemNamePrefix)) {
//              return false;
//            }
//
//            if (gemVersion == null) {
//              return true;
//            }
//            final String fileGemVersion = fileName.substring(gemNamePrefix.length() + 1);
//            return acceptHigherVersions
//                ? fileGemVersion.compareToIgnoreCase(gemVersion) >= 0
//                : fileGemVersion.equals(gemVersion);
//          }
//        });
//        if (files.length != 0) {
//          return true;
//        }
//      } catch (SecurityException e) {
//        throw new RunBuildException(e.getMessage(), e);
//      }
//    }
//    return false;
//  }

  private static RubyScriptRunner.Output executeScriptFromSource(@NotNull final Map<String, String> runParameters,
                                                       @NotNull final Map<String, String> buildParameters, String scriptSource)
      throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    final String rubyExecutable =
        ConfigurationParamsUtil.getRubyInterpreterPath(runParameters, buildParameters);

    return RubyScriptRunner.runScriptFromSource(rubyExecutable, new String[0], scriptSource, new String[0]);
  }

  /**
   * Finds script with given relative path in SDK
   * @param runParameters Run params
   * @param buildParameters Build params
   * @param scriptPath Path of given script
   * @return Full path of given script
   * @throws RakeTasksRunner.MyBuildFailureException If script will not be found
   * @throws RunBuildException Other error
   */
  private static String findSdkScript(final Map<String, String> runParameters,
                                      final Map<String, String> buildParameters,
                                      final String scriptPath) throws RakeTasksRunner.MyBuildFailureException, RunBuildException {

    final String scriptSource = GET_LOAD_PATH_SCRIPT;
    final RubyScriptRunner.Output result = executeScriptFromSource(runParameters, buildParameters, scriptSource);
    final String loadPaths[] = TextUtil.splitByLines(result.getStdout());
    for (String path : loadPaths) {
      final String fullPath = toSystemIndependentName(path + File.separatorChar + scriptPath);
      if (FileUtil.checkIfExists(fullPath)) {
        return fullPath;
      }
    }

    // Error
    final String msg = "File '" + scriptPath + "' wasn't found in $LOAD_PATH of Ruby SDK with interpreter: '" + ConfigurationParamsUtil.getRubyInterpreterPath(runParameters, buildParameters) + "'";
    throw new RakeTasksRunner.MyBuildFailureException(msg, RakeRunnerBundle.RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT);
  }
}
