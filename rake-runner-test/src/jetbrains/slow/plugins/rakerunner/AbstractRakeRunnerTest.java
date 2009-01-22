/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.BuildRunner;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.slow.PartialBuildMessagesChecker;
import jetbrains.slow.RunnerTestBase;

/**
 * @author Roman Chernyatchik
 */
public abstract class AbstractRakeRunnerTest extends RunnerTestBase {
  public AbstractRakeRunnerTest(String s) {
    super(s);
  }

  protected void finishProcess(final BuildRunner buildRunner) {
    //Do nothing
  }

  protected String getTestDataSuffixPath() {
    return "plugins/rakeRunner/";
  }

  protected BuildRunner createRunner(final File[] files) {
    return new RakeTasksRunner();
  }


  protected void setTaskNames(final String task_names) {
    myAgentRunningBuildEx.addRunnerParameter(RakeRunnerConstants.SERVER_UI_RAKE_TASKS_PROPERTY,
                                             task_names);
  }

  protected void setWorkingDir(final Map<String, String> runParameters,
                               final String relativePath) {
    runParameters.put(AgentRuntimeProperties.BUILD_WORKING_DIR,
                      getTestDataPath(relativePath).getAbsolutePath());
  }


  protected void initAndDoTest(final String task_full_name,
                              final boolean shouldPass,
                              final String testDataApp) throws Throwable {
    initAndDoTest(task_full_name, "", shouldPass, testDataApp);
  }

  protected void initAndDoTest(final String task_full_name,
                               final String result_file_suffix,
                               final boolean shouldPass,
                               final String testDataApp) throws Throwable {
    myAgentRunningBuildEx.addRunnerParameter(AgentRuntimeProperties.BUILD_WORKING_DIR,
                                             getTestDataPath(testDataApp).getAbsolutePath());
    setTaskNames(task_full_name);

    doTest(testDataApp + "/results/" + task_full_name.replace(":", "/") + result_file_suffix);
    assertEquals(shouldPass, !myBuildFailed);
  }

  protected void rakeUI_EnableTraceOption() {
    myAgentRunningBuildEx.addRunnerParameter(RakeRunnerConstants.SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED,
                                             "true");
  }

  @Override
  protected void setPartialMessagesChecker() {
    myChecker = new PartialBuildMessagesChecker() {
      private final Pattern DURATION_VALUE_PATTERN = Pattern.compile(" duration='([\\d]+)'");
      private final Pattern FAILURE_DETAILS_VALUE_PATTERN = Pattern.compile(" details='(([^']||\\|')+)[^|]'");

      @Override
      public void assertMessagesEquals(final File file,
                                       final String actual) throws Throwable {

        final String patchedActual =
            removeDetails(removeDuration(actual));
        super.assertMessagesEquals(file, patchedActual);
      }

      private String removeDetails(String actualWithoutDuration) {
        return FAILURE_DETAILS_VALUE_PATTERN.matcher(actualWithoutDuration).replaceAll(" details='##STACKTRACE##'");
      }

      private String removeDuration(String actual) {
        return DURATION_VALUE_PATTERN.matcher(actual).replaceAll(" duration='##DURATION##'");
      }
    };
  }

//////////////////////////////////////////////////
  // NUnitRunnerTestCase
  //TODO - refactor
  ////////////////////////////////////////////////////
//  protected void assertSucessful(final String... tests) {
//    final List<TestBlockBean> tests1 = buildStatistics(1).getPassedTests();
//    final String STATUS = "successful";
//
//    assertCollection(tests1, STATUS, tests);
//  }
//
//  protected void assertFailed(final String... tests) {
//    final List<TestBlockBean> tests1 = buildStatistics(1).getFailedTests();
//    final String STATUS = "failed";
//
//    assertCollection(tests1, STATUS, tests);
//  }
//
//  protected void assertIgnored(final String... tests) {
//    final List<TestBlockBean> tests1 = buildStatistics(1).getIgnoredTests();
//    final String STATUS = "ignored";
//
//    assertCollection(tests1, STATUS, tests);
//  }
//
//  protected void assertTestsCount(final int expectedCount) {
//    assertEquals(expectedCount, buildStatistics(1).getAllTestCount());
//  }
//
//  private void assertCollection(final List<TestBlockBean> tests1, final String STATUS, final String... tests) {
//    String errors = "";
//    boolean shouldFail = tests1.size() != tests.length;
//
//    if (shouldFail) {
//      errors = "There are more than expected " + STATUS + " tests";
//    }
//
//    for (String s : tests) {
//      boolean hasTest = false;
//      for (TestBlockBean testBlockBean : tests1) {
//        if (testBlockBean.getTestName().equals(s)) {
//          hasTest = true;
//          break;
//        }
//      }
//      if (!hasTest) {
//        shouldFail = true;
//
//        errors += "Test does was not " + STATUS + " " + s + "\r\n";
//      }
//    }
//
//    if (shouldFail) {
//      dumpTestsInfo();
//
//      Assert.assertFalse(shouldFail, errors);
//    }
//  }
//
//  protected void dumpTestsInfo() {
//    final BuildStatistics builder = buildStatistics(1);
//    final String report = dumpTestsList("Failed", builder.getFailedTests()) +
//                          dumpTestsList("Ignored", builder.getIgnoredTests()) +
//                          dumpTestsList("Success", builder.getPassedTests());
//
//    Loggers.TEST.warn("report = \r\n" + report);
//    System.out.println("report = " + report);
//
//  }
//
//  private String dumpTestsList(final String message, final List<TestBlockBean> bean) {
//    String str = "\r\nDump tests from " + message + ":\r\n";
//    for (TestBlockBean blockBean : bean) {
//      str += "  " + blockBean.getTestName() + "\r\n";
//    }
//    return str;
//  }
}
