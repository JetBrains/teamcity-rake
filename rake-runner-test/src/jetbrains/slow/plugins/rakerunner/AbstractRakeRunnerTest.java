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

package jetbrains.slow.plugins.rakerunner;

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import jetbrains.buildServer.messages.ServerMessagesTranslator;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.ShortStatistics;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.slow.PartialBuildMessagesChecker;
import jetbrains.slow.RunnerTest2Base;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import static jetbrains.slow.plugins.rakerunner.MockingOptions.*;

/**
 * @author Roman Chernyatchik
 */
public abstract class AbstractRakeRunnerTest extends RunnerTest2Base {
  private static final String INTERPRETER_PATH_PROPERTY = "rake-runner.ruby.interpreter.path";

  //private MockingOptions[] myCheckerMockOptions = new MockingOptions[0];
  private boolean myShouldTranslateMessages = false;

  @Override
  @NotNull
  protected String getRunnerType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    setMockingOptions(FAKE_STACK_TRACE, FAKE_LOCATION_URL, FAKE_ERROR_MSG);
    setMessagesTranslationEnabled(false);

    // set ruby interpreter path
    setInterpreterPath();

    getBuildType().addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY, RakeRunnerConstants.CURRENT_CONFIG_VERSION));
  }

  protected void setMessagesTranslationEnabled(boolean enabled) {
    myFixture.getSingletonService(ServerMessagesTranslator.class).setTranslationEnabled(enabled);
    myShouldTranslateMessages = enabled;
  }

  private void setInterpreterPath() {
    final String interpreterPath = System.getProperty(INTERPRETER_PATH_PROPERTY);
    if (!TextUtil.isEmpty(interpreterPath)) {
      getBuildType().addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER, interpreterPath));
    }
  }

  @Override
  protected File getTestDataPath(final String buildFileName) {
    return new File("svnrepo/rake-runner/rake-runner-test/testData/" + getTestDataSuffixPath() + buildFileName);
  }

  @Override
  protected String getTestDataSuffixPath() {
    return "plugins/rakeRunner/";
  }

  protected void setTaskNames(final String task_names) {
    addRunParameter(RakeRunnerConstants.SERVER_UI_RAKE_TASKS_PROPERTY, task_names);
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

  protected void initAndDoRealTest(final String task_full_name,
                                   final boolean shouldPass,
                                   final String testDataApp) throws Throwable {
    initAndDoTest(task_full_name, "_real", shouldPass, testDataApp);
  }

  protected void doTestWithoutLogCheck(final String task_full_name,
                                       final boolean shouldPass,
                                       final String testDataApp) throws Throwable {
    initAndDoTest(task_full_name, null, shouldPass, testDataApp);
  }

  protected void initAndDoTest(final String task_full_name,
                               @Nullable final String result_file_suffix,
                               final boolean shouldPass,
                               final String testDataApp) throws Throwable {

    addRunParameter(AgentRuntimeProperties.BUILD_WORKING_DIR, getTestDataPath(testDataApp).getAbsolutePath());
    setTaskNames(task_full_name);

    final String resultFileName = result_file_suffix == null
                                  ? null
                                  : testDataApp + "/results/"
                                    + task_full_name.replace(":", "/")
                                    + result_file_suffix
                                    // lets automatically expect "_log"
                                    // suffix to each translated result (build log) file
                                    + (myShouldTranslateMessages ? "_log" : "");
    doTest(resultFileName);
    assertEquals(shouldPass, !getLastFinishedBuild().getBuildStatus().isFailed());
  }

  protected void rakeUI_EnableTraceOption() {
    addRunParameter(RakeRunnerConstants.SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED, "true");
  }

  protected void setMockingOptions(final MockingOptions... options) {
    setBuildEnvironmentVariable(getEnvVarName(), getEnvVarValue(options));
  }

  protected void setPartialMessagesChecker() {
    setMessageChecker(new PartialBuildMessagesChecker() {
      //  (all except ' and |) or |' or |n or |r or || or |]
      //private final String VALUE_PATTERN = "'(([^'|]||\\|'||\\|n||\\|r||\\|\\|||\\|\\])+)'";
      //private final Pattern TIMESTAMP_VALUE_PATTERN = Pattern.compile(" timestamp=" + VALUE_PATTERN);
      //private final Pattern ERROR_DETAILS_VALUE_PATTERN = Pattern.compile(" errorDetails=" + VALUE_PATTERN);
      //private final Pattern MESSAGE_TEXT_PATTERN = Pattern.compile("message text=" + VALUE_PATTERN);
      //private final Pattern LOCATION_PATTERN = Pattern.compile("location=" + VALUE_PATTERN);
      private final Pattern VFS_FILE_PROTOCOL_PATTERN_WIN = Pattern.compile("file://");

      @Override
      public void assertMessagesEquals(final File file,
                                       final String actual) throws Throwable {

        //final String patchedActual =  mockMessageText(mockErrorDetails(mockTimeStamp(actual)));
        String patchedActual =  actual;
        if (SystemInfo.isWindows) {
          patchedActual = VFS_FILE_PROTOCOL_PATTERN_WIN.matcher(actual).replaceAll("file:");
        }

        //for (MockingOptions option : myCheckerMockOptions) {
        //  switch (option) {
        //    case FAKE_ERROR_MSG:
        //      patchedActual = mockErrorDetails(patchedActual);
        //      break;
        //    case FAKE_LOCATION_URL:
        //      patchedActual = mockLocation(patchedActual);
        //      break;
        //    case FAKE_STACK_TRACE:
        //      patchedActual = mockErrorDetails(patchedActual);
        //      break;
        //    case FAKE_TIME:
        //      patchedActual = mockTimeStamp(patchedActual);
        //      break;
        //  }
        //}
        super.assertMessagesEquals(file, patchedActual);
      }

      //private String mockErrorDetails(final String text) {
      //  return ERROR_DETAILS_VALUE_PATTERN.matcher(text).replaceAll(" errorDetails='##STACK_TRACE##'");
      //}
      //
      //private String mockTimeStamp(String actual) {
      //  return TIMESTAMP_VALUE_PATTERN.matcher(actual).replaceAll(" timestamp='##TIME##'");
      //}
      //
      //private String mockMessageText(String actual) {
      //  return MESSAGE_TEXT_PATTERN.matcher(actual).replaceAll("message text='##MESSAGE##'");
      //}
      //
      //private String mockLocation(String actual) {
      //  return LOCATION_PATTERN.matcher(actual).replaceAll("location='$LOCATION$'");
      //}
    });
  }

  protected void assertTestsCount(int succ, int failed, int ignored) {
    final ShortStatistics shortStatistics = getLastFinishedBuild().getShortStatistics();
    final int aSucc = shortStatistics.getPassedTestCount();
    final int aFailed = shortStatistics.getFailedTestCount();
    final int aIgnored = shortStatistics.getIgnoredTestCount();

    try {
      Assert.assertEquals(aSucc, succ, "success");
      Assert.assertEquals(aFailed, failed, "failed");
      Assert.assertEquals(aIgnored, ignored, "ignored");
    } catch (Throwable e) {
      System.out.println("aSucc = " + aSucc);
      System.out.println("aFailed = " + aFailed);
      System.out.println("aIgnored = " + aIgnored);
      throw new RuntimeException(e);
    }
  }

  protected void activateTestFramework(@NotNull SupportedTestFramework framework) {
    getBuildType().addRunParameter(new SimpleParameter(framework.getFrameworkUIProperty(), "true"));
  }

  @Override
  protected String doRunnerSpecificReplacement(final String expected) {
    String msg = expected.replaceAll("[0-9]{4}-[0-9]{2}-[0-9]{2}('T'|T)[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}[+\\-]{1}[0-9]+", "##TIME##");
    msg = msg.replaceAll("duration='[0-9]+'", "duration='##DURATION##'");
    return msg;
  }
}
