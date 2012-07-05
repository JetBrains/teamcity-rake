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

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import jetbrains.buildServer.PartialBuildMessagesChecker;
import jetbrains.buildServer.RunnerTest2Base;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.messages.BuildMessagesProcessor;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.ShortStatistics;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import static jetbrains.buildServer.messages.serviceMessages.ServiceMessage.SERVICE_MESSAGE_START;
import static jetbrains.slow.plugins.rakerunner.MockingOptions.*;

/**
 * @author Roman Chernyatchik
 */
public abstract class AbstractRakeRunnerTest extends RunnerTest2Base {

  //private MockingOptions[] myCheckerMockOptions = new MockingOptions[0];
  private boolean myShouldTranslateMessages = false;
  private String myRubyVersion;
  private String myGemfileName;
  private final Set<String> myFilesToDelete = new HashSet<String>();


  @Override
  @NotNull
  protected String getRunnerType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }

  @BeforeMethod(dependsOnMethods = "setRubyVersion")
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    setMockingOptions(FAKE_STACK_TRACE, FAKE_LOCATION_URL, FAKE_ERROR_MSG);
    setMessagesTranslationEnabled(false);
    if (myRubyVersion == null) {
      if (SystemInfo.isWindows) {
        setInterpreterPath();
      } else if (SystemInfo.isUnix) {
        setRVMConfiguration();
      }
    } else {
      if (SystemInfo.isWindows) {
        setInterpreterPath(myRubyVersion);
      } else if (SystemInfo.isUnix) {
        setRVMConfiguration(myRubyVersion);
      }
    }

    getBuildType().addRunParameter(
      new SimpleParameter(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY, RakeRunnerConstants.CURRENT_CONFIG_VERSION));
  }

  @BeforeMethod
  @Parameters({"ruby.version"})
  protected void setRubyVersion(@NotNull final String rubyVersion) throws Throwable {
    myRubyVersion = rubyVersion;
  }

  protected void setMessagesTranslationEnabled(boolean enabled) {
    //TODO: Do not use this to disable service messages translation
    myFixture.getSingletonService(BuildMessagesProcessor.class).setTranslationEnabled(enabled);
    myShouldTranslateMessages = enabled;
  }

  private void setInterpreterPath() {
    RakeRunnerTestUtil.setInterpreterPath(getBuildType());
  }

  private void setInterpreterPath(@NotNull final String rubyVersion) throws RakeRunnerTestUtil.InterpreterNotFoundException {
    RakeRunnerTestUtil.setInterpreterPath(getBuildType(), rubyVersion);
  }

  private void setRVMConfiguration() {
    RakeRunnerTestUtil.setRVMConfiguration(getBuildType());
  }

  private void setRVMConfiguration(@NotNull final String rubySdkName) {
    RakeRunnerTestUtil.setRVMConfiguration(getBuildType(), rubySdkName);
  }

  protected void useRVMRubySDK(@NotNull String sdkname) {
    RakeRunnerTestUtil.useRVMRubySDK(sdkname, getBuildType());
  }

  protected void useRVMGemSet(@NotNull String gemset) {
    RakeRunnerTestUtil.useRVMGemSet(gemset, getBuildType());
  }

  protected void useBundleGemfile(@NotNull final String gemfileFolder) {
    myGemfileName = gemfileFolder;
    RakeRunnerTestUtil.useBundleExec(getBuildType());
  }

  @Override
  protected File getTestDataPath(final String buildFileName) {
    return RakeRunnerTestUtil.getTestDataItemPath(getTestDataSuffixPath() + buildFileName);
  }

  @Override
  protected String getTestDataSuffixPath() {
    return "plugins/rakeRunner/";
  }

  protected void setTaskNames(final String task_names) {
    addRunParameter(RakeRunnerConstants.SERVER_UI_RAKE_TASKS_PROPERTY, task_names);
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  protected void initAndDoTest(final String task_full_name,
                               @Nullable final String result_file_suffix,
                               final boolean shouldPass,
                               final String testDataApp) throws Throwable {

    final String workingDirectory = getTestDataPath(testDataApp).getAbsolutePath();
    if (myGemfileName != null) {
      // Copy Gemfile
      final File gemfile = new File(workingDirectory, "Gemfile");
      FileUtil.copy(new File(getTestDataPath("gemfiles"), myGemfileName), gemfile);
      myFilesToDelete.add(gemfile.getAbsolutePath());
      myFilesToDelete.add(gemfile.getAbsolutePath() + ".lock");
      getBuildType().addBuildParameter(new SimpleParameter(RakeRunnerConstants.CUSTOM_GEMFILE_RELATIVE_PATH, gemfile.getAbsolutePath()));
    }
    addRunParameter(AgentRuntimeProperties.BUILD_WORKING_DIR, workingDirectory);
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
    final List<LogMessage> errorMessages = getLastFinishedBuild().getBuildLog().getErrorMessages();
    assertEquals(errorMessages.toString(), shouldPass, !getLastFinishedBuild().getBuildStatus().isFailed());
  }


  @AfterClass
  public void removeBundleFiles() throws Throwable {
    for (final String path : myFilesToDelete) {
      FileUtil.delete(new File(path));
    }
  }

  protected void rakeUI_EnableTraceOption() {
    addRunParameter(RakeRunnerConstants.SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED, "true");
  }

  protected void setMockingOptions(final MockingOptions... options) {
    setBuildEnvironmentVariable(getEnvVarName(), getEnvVarValue(options));
  }

  @Override
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
        String patchedActual = actual;
        if (SystemInfo.isWindows) {
          patchedActual = VFS_FILE_PROTOCOL_PATTERN_WIN.matcher(actual).replaceAll("file:");
        }
        patchedActual = patchedActual.replaceAll(" +", " ");
//        patchedActual = patchedActual.replace("RSpec", "Spec");

        patchedActual = reorderAttributesOfServiceMessages(patchedActual);

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

  private static String reorderAttributesOfServiceMessages(String patchedActual) {
    final String[] lines = patchedActual.split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];

      final int serviceMessageStart = line.indexOf(SERVICE_MESSAGE_START);
      if (serviceMessageStart != -1) {
        // Fix order of attributes to match order of attributes in the test data.
        // Just to avoid patching all the test data.

        String text = line.substring(serviceMessageStart + SERVICE_MESSAGE_START.length());
        String prefix = line.substring(0, serviceMessageStart + SERVICE_MESSAGE_START.length() + text.indexOf(" ") + 1);
        text = text.substring(text.indexOf(" "), text.lastIndexOf(']')).trim();
        if (text.startsWith("'")) {
          // In case we have 'Single attribute message'
          // do nothing
        } else {
          // In case we located at least one attribute ('Multiple attribute message')
          try {
            final Map<String, String> attributes = StringUtil.stringToProperties(text, StringUtil.STD_ESCAPER2, false);
            final ArrayList<String> keys = new ArrayList<String>(attributes.keySet());
            reorderAttributes(keys);
            final Map<String, String> orderedAttributes = new LinkedHashMap<String, String>();
            for (String key : keys) {
              orderedAttributes.put(key, attributes.get(key));
            }
            final String s = StringUtil.propertiesToString(orderedAttributes, StringUtil.STD_ESCAPER2);

            String newline = prefix + s + line.substring(line.lastIndexOf(']'));
            lines[i] = newline;
          } catch (ParseException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    patchedActual = StringUtil.join("\n", lines);
    return patchedActual;
  }

  private static void reorderAttributes(final List<String> foundAttrs) {
    final String[] sequence = {"name", "captureStandardOutput", "locationHint", "message", "details", "error",
      "text", "status", "errorDetails", "type", "duration", "timestamp"
    };
    int ii = 0;
    for (String attrName : sequence) {
      for (int j = ii; j < foundAttrs.size(); j++) {
        if (foundAttrs.get(j).equals(attrName)) {
          swap(foundAttrs, ii, j);
          ii++;
          break;
        }
      }
    }
  }

  private static void swap(final List<String> foundAttrs, final int i, final int j) {
    if (j == i) return;
    String ival = foundAttrs.get(i);
    foundAttrs.set(i, foundAttrs.get(j));
    foundAttrs.set(j, ival);
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

  protected void activateTestFramework(@NotNull final SupportedTestFramework... frameworks) {
    for (SupportedTestFramework framework : frameworks) {
      activateTestFramework(framework);
    }
  }

  protected void activateTestFramework(@NotNull final SupportedTestFramework framework) {
    getBuildType().addRunParameter(new SimpleParameter(framework.getFrameworkUIProperty(), "true"));
  }

  @Override
  protected String doRunnerSpecificReplacement(final String expected) {
    String msg = expected.replaceAll("[0-9]{4}-[0-9]{2}-[0-9]{2}('T'|T)[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}[+\\-]{1}[0-9]+", "##TIME##");
    msg = msg.replaceAll("duration ?= ?'[0-9]+'", "duration='##DURATION##'");
    return msg;
  }
}
