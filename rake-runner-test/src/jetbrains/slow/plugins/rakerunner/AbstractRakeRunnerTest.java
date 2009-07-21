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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.BuildRunner;
import jetbrains.buildServer.agent.rakerunner.RakeTasksRunner;
import jetbrains.buildServer.agent.rakerunner.utils.TextUtil;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.ServerMessagesTranslator;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.impl.RunningBuildImpl;
import jetbrains.slow.PartialBuildMessagesChecker;
import jetbrains.slow.RunnerTestBase;
import static jetbrains.slow.plugins.rakerunner.MockingOptions.*;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.BeforeMethod;
import com.intellij.openapi.util.SystemInfo;

/**
 * @author Roman Chernyatchik
 */
public abstract class AbstractRakeRunnerTest extends RunnerTestBase {
  private static final String INTERPRETER_PATH_PROPERTY = "rake-runner.ruby.interpreter.path";

  //private MockingOptions[] myCheckerMockOptions = new MockingOptions[0];
  protected boolean myShouldTranslateMessages = false;

  public AbstractRakeRunnerTest(String s) {
    super(s);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setMockingOptions(FAKE_TIME, FAKE_STACK_TRACE, FAKE_LOCATION_URL, FAKE_ERROR_MSG);
  }

  @Override
  protected void appendRunnerSpecificRunParameters(Map<String, String> runParameters) throws IOException, RunBuildException {
    // set ruby interpreter path
    setInterpreterPath(runParameters);

    // configuration version
    runParameters.put(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY,
                      RakeRunnerConstants.CURRENT_CONFIG_VERSION);

  }

  protected void setInterpreterPath(final Map<String, String> runParameters) {
    final String interpreterPath = System.getProperty(INTERPRETER_PATH_PROPERTY);
    if (!TextUtil.isEmpty(interpreterPath)) {
      runParameters.put(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER,
                        interpreterPath);
    }
  }

  protected List<BuildMessage1> translateMessages(final ArrayList<BuildMessage1> result, final RunningBuildImpl runningBuild) {
    if (myShouldTranslateMessages) {
      return myServerCreator.getSingletonService(ServerMessagesTranslator.class).translateMessages(result, runningBuild);
    }
    return result;
  }

  protected File getTestDataPath(final String buildFileName) {
    return new File("svnrepo/rake-runner/rake-runner-test/testData/" + getTestDataSuffixPath() + buildFileName);
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
    myAgentRunningBuildEx.addRunnerParameter(AgentRuntimeProperties.BUILD_WORKING_DIR,
                                             getTestDataPath(testDataApp).getAbsolutePath());
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
    assertEquals(shouldPass, !myBuildFailed);
  }

  protected void rakeUI_EnableTraceOption() {
    myAgentRunningBuildEx.addRunnerParameter(RakeRunnerConstants.SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED,
                                             "true");
  }

  protected void setMockingOptions(final MockingOptions... options) {
    //myCheckerMockOptions = options;
    MockingOptions.addToBuildParams(options,
                                    myAgentRunningBuildEx.getModifiableBuildParameters());
  }

  @Override
  protected void setPartialMessagesChecker() {
    myChecker = new PartialBuildMessagesChecker() {
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
    };
  }
}
