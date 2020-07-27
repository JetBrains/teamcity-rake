/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import jetbrains.buildServer.PartialBuildMessagesChecker;
import jetbrains.buildServer.RunnerTest2Base;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.FlowLogger;
import jetbrains.buildServer.agent.ServerProvidedProperties;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.BuildMessagesProcessor;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.RunningBuildEx;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ShortStatistics;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static jetbrains.buildServer.messages.serviceMessages.ServiceMessage.SERVICE_MESSAGE_START;
import static jetbrains.slow.plugins.rakerunner.MockingOptions.*;

/**
 * @author Roman Chernyatchik
 */
public abstract class AbstractRakeRunnerTest extends RunnerTest2Base implements ITest {

  static {
    final Logger logger = Logger.getLogger("jetbrains.slow.plugins.rakerunner");
    logger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
    logger.setLevel(Level.WARN);
  }

  //private MockingOptions[] myCheckerMockOptions = new MockingOptions[0];
  private boolean myShouldTranslateMessages = false;
  private String myRubyVersion;
  private final Set<String> myFilesToDelete = new HashSet<String>();
  private static File ourTempsContainerDir;
  private File myWorkingDirectory;

  protected AbstractRakeRunnerTest() {
    setName(this.getClass().getSimpleName());
  }

  @Override
  @NotNull
  protected String getRunnerType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    setAgentOwnPort();
    super.setUp1();
    setMockingOptions(FAKE_STACK_TRACE, FAKE_LOCATION_URL, FAKE_ERROR_MSG);
    setMessagesTranslationEnabled(false);
    if (myRubyVersion == null) {
      if (SystemInfo.isWindows) {
        setInterpreterPath();
      } else if (SystemInfo.isUnix) {
        setRubyConfiguration();
      }
    } else {
      if (SystemInfo.isWindows) {
        setInterpreterPath(myRubyVersion);
      } else if (SystemInfo.isUnix) {
        setRubyConfiguration(myRubyVersion);
      }
    }

    getBuildType().addRunParameter(
      new SimpleParameter(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY, RakeRunnerConstants.CURRENT_CONFIG_VERSION));
    getBuildType().addRunParameter(
      new SimpleParameter(ServerProvidedProperties.TEAMCITY_VERSION_ENV, System.getenv("TEAMCITY_VERSION")));
  }

  private static int ourAgentOwnPort = 0;
  private static void setAgentOwnPort() {
    if (ourAgentOwnPort != 0) {
      return;
    }
    String property = System.getProperty(AgentRuntimeProperties.OWN_PORT);
    if (property == null) {
      ourAgentOwnPort = 9090;
    }
    try {
      ourAgentOwnPort = Integer.parseInt(property);
      if (ourAgentOwnPort == 12345) ourAgentOwnPort = 9090;
    } catch (NumberFormatException e) {
      ourAgentOwnPort = 9090;
    }
  }

  public static int getAgentOwnPort() {
    if (ourAgentOwnPort == 0) {
      new Throwable("AbstractRakeRunnerTest.getAgentOwnPort called to early").printStackTrace(System.err);
      return 9090;
    }
    return ourAgentOwnPort;
  }

  protected void setRubyVersion(@NotNull final String rubyVersion) {
    myRubyVersion = rubyVersion;
  }

  protected String getRubyVersion() {
    return myRubyVersion;
  }

  protected void setMessagesTranslationEnabled(boolean enabled) {
    System.getProperties().remove(BuildMessagesProcessor.TEAMCITY_BUILD_MESSAGES_TRANSLATION_ENABLED_PROP);
    if (!enabled) {
      System.setProperty(BuildMessagesProcessor.TEAMCITY_BUILD_MESSAGES_TRANSLATION_ENABLED_PROP, "false");
    }
    myShouldTranslateMessages = enabled;
  }

  private void setInterpreterPath() throws RakeRunnerTestUtil.InterpreterNotFoundException {
    RakeRunnerTestUtil.setInterpreterPath(getBuildType());
  }

  private void setInterpreterPath(@NotNull final String rubyVersion) throws RakeRunnerTestUtil.InterpreterNotFoundException {
    RakeRunnerTestUtil.setInterpreterPath(getBuildType(), rubyVersion);
  }

  private void setRubyConfiguration() {
    if (RakeRunnerTestUtil.isUseRVM()) {
      RakeRunnerTestUtil.setRVMConfiguration(getBuildType());
    } else if (RakeRunnerTestUtil.isUseRbEnv()) {
      RakeRunnerTestUtil.setRbEnvConfiguration(getBuildType());
    }
  }

  private void setRubyConfiguration(@NotNull final String rubySdkName) {
    if (RakeRunnerTestUtil.isUseRVM()) {
      RakeRunnerTestUtil.setRVMConfiguration(getBuildType(), rubySdkName);
    } else if (RakeRunnerTestUtil.isUseRbEnv()) {
      RakeRunnerTestUtil.setRbEnvConfiguration(getBuildType(), rubySdkName);
    }
  }

  protected void useRVMRubySDK(@NotNull String sdkname) {
    RakeRunnerTestUtil.useRVMRubySDK(sdkname, getBuildType());
  }

  protected void useRVMGemSet(@NotNull String gemset) {
    RakeRunnerTestUtil.useRVMGemSet(gemset, getBuildType());
  }

  protected void setUseBundle(final boolean use) {
    RakeRunnerTestUtil.useBundleExec(getBuildType(), use);
  }

  @Override
  protected File getTestDataPath(final String buildFileName) {
    return RakeRunnerTestUtil.getTestDataItemPath(getTestDataSuffixPath() + buildFileName);
  }

  @Override
  protected String getTestDataSuffixPath() {
    return "plugins/rakeRunner/";
  }

  public static File getTempsContainerDir() throws IOException {
    if (ourTempsContainerDir == null) {
      synchronized (AbstractRakeRunnerTest.class) {
        if (ourTempsContainerDir == null) {
          ourTempsContainerDir = FileUtil.createTempDirectory("rake-runner-temp-container", null);
          //ourTempsContainerDir = FileUtil.createEmptyDir(RakeRunnerTestUtil.getTestDataItemPath("temp-container"));
        }
      }
    }
    return ourTempsContainerDir;
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

  protected void initAndDoTest(final String task_full_name,
                               @Nullable final String result_file_suffix,
                               final boolean shouldPass,
                               final String testDataApp) throws Throwable {

    final File workingDirectory = getTestDataPath(testDataApp).getAbsoluteFile();
    initAndDoTest(task_full_name, result_file_suffix, shouldPass, testDataApp, workingDirectory);
  }

  protected void initAndDoTest(final String task_full_name,
                               @Nullable final String result_file_suffix,
                               boolean shouldPass,
                               @NotNull final String testDataApp,
                               @NotNull final File workingDirectory) throws Throwable {
    myWorkingDirectory = workingDirectory;
    addRunParameter(AgentRuntimeProperties.BUILD_WORKING_DIR, workingDirectory.getAbsolutePath());
//    addRunParameter(AgentRuntimeProperties.BUILD_CHECKOUT_DIR, workingDirectory.getAbsolutePath());
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


  @AfterMethod(alwaysRun = true)
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
        String patchedActual = actual.trim();
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
      String line = lines[i].trim();

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
            removeAttributes(attributes);
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

  private static void removeAttributes(Map<String, String> attributes) {
    for(String trimed: Arrays.asList("nodeId", "parentNodeId")) {
      attributes.remove(trimed);
    }
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
    assertTestsCount(succ, failed, ignored, getLastFinishedBuild().getShortStatistics());
  }

  protected void assertTestsCount(int succ, int failed, int ignored, ShortStatistics shortStatistics) {
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
    msg = msg.replaceAll("duration ?= ?'[0-9]*\\Q##OWN_PORT##\\E[0-9]*'", "duration='##DURATION##'");
    if (myWorkingDirectory != null) {
      final String rel = FileUtil.getRelativePath(getCurrentDir().getAbsoluteFile(), myWorkingDirectory.getAbsoluteFile());
      if (rel != null) {
        if (rel.startsWith("..")) {
          msg = replacePath(msg, myWorkingDirectory.getAbsoluteFile(), "##WORKING_DIR##");
        } else {
          msg = msg.replaceAll(Pattern.quote(rel), "##WORKING_DIR##");
        }
      }
    }
    return msg;
  }

  protected void doPrepareGemset(@NotNull final String version, @NotNull final String gemset, @NotNull final Logger LOG, @NotNull final File gemfile) throws IOException {
    if (!SystemInfo.isUnix) {
      return;
    }
    final File cacheDir = getTestDataPath("gems/vendor/cache");
    FileUtil.createDir(cacheDir);
    final File workingDirectory = gemfile.getParentFile();
    final List<String> commands = new ArrayList<String>();
    if (RakeRunnerTestUtil.isUseRVM()) {
      commands.add("source " + getTestDataPath("gems/checkRVMCommand.sh").getAbsolutePath());
      commands.add("checkRVMCommand");
      commands.add(String.format("rvm use \"%s@%s\" --create", version, gemset));
    } else if (RakeRunnerTestUtil.isUseRbEnv()) {
      commands.add("rbenv local " + version);
    } else {
      throw new IllegalStateException("Expected to be run on machine with either RVM or RbEnv installed.");
    }
    Collections.addAll(commands,
                       "[[ ! -d 'vendor' ]] && mkdir vendor",
                       "[[ ! -d 'vendor/cache' ]] && ln -s '" + cacheDir.getAbsolutePath() + "' 'vendor/cache'",
                       "gem which bundler || gem install bundler",
                       "rm -f Gemfile.lock",
                       "bundle install --local --no-prune || (rm -f Gemfile.lock; bundle install --no-prune)",
                       "bundle update",
                       "bundle package --no-prune");
    RunCommandsHelper.runBashScript(LOG, workingDirectory, commands.toArray(new String[commands.size()]));
    //FileUtil.copyDir(localCacheDir, cacheDir);
    try {
      final FlowLogger fl = LogUtil.getFlowLogger(LOG);
      fl.activityStarted("Actual Gemfile.lock:", "AGL");
      fl.message(FileUtil.readText(new File(gemfile.getParent(), "Gemfile.lock")));
      fl.activityFinished("Actual Gemfile.lock:", "AGL");
    } catch (Exception ignored) {
    }
  }

  protected void doInstallBundlerGem(@NotNull final Logger LOG) throws IOException {
    if (!SystemInfo.isUnix) {
      return;
    }

    final String sdk = getRunnerParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME);
    final String gs = getRunnerParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME);
    if (RakeRunnerTestUtil.isUseRVM()) {
      RunCommandsHelper.runBashScript(LOG, getTestDataPath("gems/checkRVMCommand.sh").getParentFile(),
                                      "source " + getTestDataPath("gems/checkRVMCommand.sh").getAbsolutePath(),
                                      "checkRVMCommand",
                                      StringUtil.isEmptyOrSpaces(gs) ? String.format("rvm use \"%s\"", sdk) : String.format("rvm use \"%s\" --create", sdk + "@" + gs),
                                      "gem which bundler || gem install bundler"
      );
    } else if (RakeRunnerTestUtil.isUseRbEnv()) {
      RunCommandsHelper.runBashScript(LOG, getTempsContainerDir(),
                                      "rbenv shell " + sdk,
                                      "gem which bundler || gem install bundler"
      );
    } else {
      throw new IllegalStateException("Expected to be run on machine with either RVM or RbEnv installed.");
    }
  }

  public String getTestName() {
    final String base = getClass().getName();
    final List<String> parametersList = getTestNameParametersList();
    if (parametersList.isEmpty()) {
      return base;
    }
    return base + "(" + StringUtil.join(",", parametersList) + ")";
  }

  @NotNull
  protected List<String> getTestNameParametersList() {
    if (myRubyVersion == null) {
      return new ArrayList<String>();
    }
    return new ArrayList<String>(Arrays.asList(myRubyVersion.replaceAll("\\-p\\d+", "")));
  }

  /**
   * BuildMessagesProcessor which do nothing
   */
  private static class AsIsBuildMessagesProcessor extends BuildMessagesProcessor {
    public AsIsBuildMessagesProcessor(SBuildServer server) {
      super(server);
    }

    @NotNull
    @Override
    public List<BuildMessage1> translateMessages(@NotNull final List<BuildMessage1> initial, @NotNull final RunningBuildEx runningBuild) {
      return initial;
    }
  }
}
