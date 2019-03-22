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

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jetbrains.buildServer.AgentServerFunctionalTestCase;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.feature.RubyEnvConfiguratorService;
import jetbrains.buildServer.agent.impl.SpringContextFixture;
import jetbrains.buildServer.agent.rakerunner.SharedParams;
import jetbrains.buildServer.agent.rakerunner.SharedParamsType;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.asserts.CommonAsserts;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConfiguration;
import jetbrains.buildServer.feature.RubyEnvConfiguratorConstants;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Roman.Chernyatchik
 */
@TestFor(testForClass = {RubyEnvConfiguratorService.class})
@Test(groups = {"all", "slow"})
@SpringContextFixture(configs = {"classpath*:/META-INF/build-agent-plugin-rakerunner.xml"})
public class RubyEnvConfiguratorServiceAgentTest extends AgentServerFunctionalTestCase {
  private final String RUN_TYPE = "mySomeRunner!";

  private static enum FakeBuildConfiguration {
    Nothing,
    Feature,
    FeatureAndFakeRvmHome // And fake RVM home
  }

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();

    createNonBlockingBuildRunner(RUN_TYPE);
  }

  @Test
  public void testRunnerPatchingEnabled() throws IOException {
    final boolean patcherEnabled = runCmdlinePatcherTest(FakeBuildConfiguration.Feature);

    Assert.assertTrue(patcherEnabled, "RubyEnvConfiguratorService should be enabled");
  }

  @Test
  public void testRunnerPatchingDisabled() throws IOException {
    final boolean patcherEnabled = runCmdlinePatcherTest(FakeBuildConfiguration.Nothing);

    Assert.assertFalse(patcherEnabled, "RubyEnvConfiguratorService should not be enabled");
  }

  private boolean runCmdlinePatcherTest(FakeBuildConfiguration configuration) throws IOException {
    final AtomicBoolean patcherEnabled = new AtomicBoolean(false);

    getExtensionHolder().registerExtension(BuildRunnerPrecondition.class, "aaa",
                                           new RubyEnvConfiguratorService(EventDispatcher.create(AgentLifeCycleListener.class)) {
                                             @Override
                                             protected EnvironmentPatchableMap patchRunnerEnvironment(@NotNull final BuildRunnerContext context,
                                                                                                      @NotNull final RubySdk sdk,
                                                                                                      @NotNull final RubyEnvConfiguratorConfiguration configuration,
                                                                                                      @NotNull final SharedParams sharedParams) {
                                               patcherEnabled.set(true);
                                               return null;
                                             }
                                           });

    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();

    // set ruby interpreter path
    final String interpreterPath = RakeRunnerTestUtil.getTestDataItemPath(".rvm/rubies/ruby-1.8.7-p352/bin/ruby").getAbsolutePath();
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, interpreterPath);

    // configure build
    final SBuildType bt = configureFakeBuild(configuration, featureParamsMap);

    // launch build
    finishBuild(startBuild(bt, false));
    return patcherEnabled.get();
  }


  @Test(groups = {"unix"})
  public void test_setSharedOptionsRvm() throws IOException {
    if (!SystemInfo.isUnix) {
      throw new SkipException("Not a UNIX. RVM support is only for Unix.");
    }
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, null, contextRef);

    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();

    // use rvm
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "manual");
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY, RubyVersionsDataProvider.getExistentRVMRubyVersion());
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY, "teamcity");
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS, "true");

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, featureParamsMap);

    // launch
    finishBuild(startBuild(bt, false));

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    SharedParams sharedParams = SharedParams.fromRunParameters(params);

    assertTrue(sharedParams.isSetted());
    assertEquals(SharedParamsType.RVM, sharedParams.getType());

    assertEquals(RubyVersionsDataProvider.getExistentRVMRubyVersion(), sharedParams.getRVMSdkName());
    assertEquals("teamcity", sharedParams.getRVMGemsetName());
    assertNull(sharedParams.getInterpreterPath());
    assertNull(sharedParams.getRVMRCPath());

    assertTrue("RVM shared settings are applied", sharedParams.isApplied());
  }

  @Test
  public void test_setSharedOptionsNoRvm() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, null, contextRef);

    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();

    final String interpreterPath =
      RakeRunnerTestUtil.getTestDataItemPath(".rvm/rubies/ruby-1.8.7-p352/bin/ruby").getAbsolutePath();
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, interpreterPath);

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, featureParamsMap);

    // launch
    try {
      finishBuild(startBuild(bt, false));
    } catch (Exception e) {
      // it is ok, our fake ruby interpreter isn't executable
    }

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    SharedParams sharedParams = SharedParams.fromRunParameters(params);

    assertTrue(sharedParams.isSetted());
    assertEquals(SharedParamsType.INTERPRETER_PATH, sharedParams.getType());

    assertEquals(interpreterPath, sharedParams.getInterpreterPath());
    assertNull(sharedParams.getRVMSdkName());
    assertNull(sharedParams.getRVMGemsetName());
    assertNull(sharedParams.getRVMRCPath());

    assertTrue(sharedParams.isApplied());
  }

  @Test
  public void testBadSdkDoesntFailBuild() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, null, contextRef);

    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, "this path doesn't exist");

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, featureParamsMap);

    // default : RubyEnvConfiguratorUtil.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY = "false"

    // launch
    final SFinishedBuild build = finishBuild(startBuild(bt, false));

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    SharedParams sharedParams = SharedParams.fromRunParameters(params);

    assertTrue(sharedParams.isSetted());
    assertFalse(sharedParams.isApplied());

    assertSuccessful(build);
  }

  @Test
  public void testSdkInPathNotFound() throws Exception {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();
    addBuildParamsListener(null, null, null, contextRef);

    final HashMap<String, String> feature = new HashMap<String, String>();
    feature.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "path");
    feature.put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, "");
    feature.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, feature);

    bt.addParameter(new SimpleParameter(Constants.ENV_PREFIX + OSUtil.getPATHEnvVariableKey(), ""));

    // launch
    final SFinishedBuild build = finishBuild(startBuild(bt, false));

    assertFailed(build);
    assertNotNull(build.getFirstInternalError());
    assertNotNull(build.getFirstInternalErrorMessage());
    assertEquals("Unable to find Ruby interpreter in PATH.", build.getFirstInternalErrorMessage());

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    SharedParams sharedParams = SharedParams.fromRunParameters(params);

    assertTrue(sharedParams.isSetted());

    assertEquals(SharedParamsType.INTERPRETER_PATH, sharedParams.getType());
    assertTrue(StringUtil.isEmpty(sharedParams.getInterpreterPath()));

    assertFalse(sharedParams.isApplied());
  }

  @Test
  @TestFor(issues = "TW-35712")
  public void testSdkInPathFound() throws Exception {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();
    addBuildParamsListener(null, null, null, contextRef);

    final HashMap<String, String> feature = new HashMap<String, String>();
    feature.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "path");
    feature.put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, "");
    feature.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, feature);

    final String interpreter = RakeRunnerTestUtil.getTestDataItemPath("fake-ruby/").getAbsolutePath();
    bt.addParameter(new SimpleParameter(Constants.ENV_PREFIX + OSUtil.getPATHEnvVariableKey(), interpreter));

    // launch
    final SFinishedBuild build = finishBuild(startBuild(bt, false));

    assertSuccessful(build);
    assertNull(build.getFirstInternalError());
    assertNull(build.getFirstInternalErrorMessage());

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    SharedParams sharedParams = SharedParams.fromRunParameters(params);

    assertTrue(sharedParams.isSetted());

    assertEquals(SharedParamsType.INTERPRETER_PATH, sharedParams.getType());
    assertTrue(StringUtil.isEmpty(sharedParams.getInterpreterPath()));

    assertTrue(sharedParams.isApplied());
  }

  @Test
  public void testBadSdkFailsBuild() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, null, contextRef);

    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();

    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, "this path doesn't exist");
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, "true");

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, featureParamsMap);

    // launch
    final SFinishedBuild build = finishBuild(startBuild(bt, false));

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    SharedParams sharedParams = SharedParams.fromRunParameters(params);

    assertTrue(sharedParams.isSetted());
    assertFalse(sharedParams.isApplied());

    assertFailed(build);
  }

  @Test(groups = {"unix"})
  public void test_RvmSdk_EnvSettings() throws Exception {
    if (!SystemInfo.isUnix) {
      // TODO: use mocks
      throw new SkipException("Not a UNIX. RVM support is only for Unix.");
    }

    final Ref<Map<String, String>> allParamsRef = new Ref<Map<String, String>>();
    final Ref<Map<String, String>> envParamsRef = new Ref<Map<String, String>>();
    final Ref<Map<String, String>> sysPropertiesRef = new Ref<Map<String, String>>();

    // add listener
    addBuildParamsListener(envParamsRef, sysPropertiesRef, allParamsRef, null);

    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();

    // use rvm
    //final String rvmRubyName = myRubyVersion != null ? myRubyVersion : System.getProperty(RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY);
    final String rvmRubyName = RubyVersionsDataProvider.getExistentRVMRubyVersion();
    final String rvmGemsetName = "teamcity";

    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "manual");
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY, rvmRubyName);
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY, rvmGemsetName);
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS, "true");

    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, featureParamsMap);

    SBuild build = startBuild(bt, false);
    build = finishBuild(build);
    dumpBuildLog(build);

    final Map<String, String> envs = envParamsRef.get();
    Assert.assertNotNull(envs);

    Assert.assertNotNull(allParamsRef.get());
    RVMPathsSettings.getInstanceEx().initialize(envParamsRef.get());
    assertNotNull(RVMPathsSettings.getInstance());
    final InstalledRVM rvm = RVMPathsSettings.getInstance().getRVM();
    assertNotNull("RVM found", rvm);
    final String rvmHomePath = rvm.getPath();

    Assert.assertNotNull(rvmHomePath, "Cannot retrieve RVM home path");

    // Ensure new gemset created
    final Pair<String, String> detected = RVMSupportUtil.determineSuitableRVMSdkDist(rvmRubyName, rvmGemsetName);
    Assert.assertNotNull(detected);
    Assert.assertNotNull(detected.first, "RVM should have interpreter " + rvmRubyName);
    Assert.assertNotNull(detected.second, "RVM should have gemset '" + rvmGemsetName + "' for interpreter " + rvmRubyName);

    // Use some regex
    final String regexRuby = "[^/]*" + rvmRubyName + "[^/@]*";
    final String regexRubyAndGemset = regexRuby + "@" + rvmGemsetName;
    final String regexRubyAndGlobal = regexRuby + "@global";

    final String envGemPath = envs.get("GEM_PATH");
    {
      Assert.assertNotNull(envGemPath, "GEM_PATH not set");
      final String[] paths = envGemPath.split(File.pathSeparator);
      CommonAsserts.then(paths).hasSize(2);

      System.out.println("GEM_PATH: " + Arrays.toString(paths));
      CommonAsserts.then(paths[0]).matches(rvmHomePath + "/gems/" + regexRubyAndGemset);
      CommonAsserts.then(paths[1]).matches(rvmHomePath + "/gems/" + regexRubyAndGlobal);
    }

    final String envGemHome = envs.get("GEM_HOME");
    {
      CommonAsserts.then(envGemHome).matches(rvmHomePath + "/gems/" + regexRubyAndGemset);
    }

    final String envPath = envs.get("PATH");
    {
      Assert.assertNotNull(envPath, "PATH not set");
      final String[] paths = envPath.split(File.pathSeparator);
      Assert.assertTrue(paths.length >= 4);

      System.out.println("PATH[0-4]: " + Arrays.asList(paths).subList(0, 4));

      CommonAsserts.then(paths[0]).matches(rvmHomePath + "/gems/" + regexRubyAndGemset + "/bin");
      CommonAsserts.then(paths[1]).matches(rvmHomePath + "/gems/" + regexRubyAndGlobal + "/bin");
      CommonAsserts.then(paths[2]).matches(rvmHomePath + "/rubies/" + regexRuby + "/bin");
      CommonAsserts.then(paths[3]).isEqualTo(rvmHomePath + "/bin");
    }

    final String envMyRubyHome = envs.get("MY_RUBY_HOME");
    {
      CommonAsserts.then(envMyRubyHome).isNotNull().matches(rvmHomePath + "/rubies/" + regexRuby);
    }

//    final String envBundlePath = envs.get("BUNDLE_PATH");
//    {
//      Assert.assertNotNull(envBundlePath);
//      Assert.assertTrue(envBundlePath.matches(rvmHomePath + "/gems/" + regexRubyAndGemset));
//    }

    final String envRVMRubyString = envs.get("rvm_ruby_string");
    {
      CommonAsserts.then(envRVMRubyString).isNotNull().matches(regexRuby);
    }

//    Assert.assertEquals(envs.get("GEM_PATH"), rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity:"
//        + rvmHomePath + "/gems/ruby-1.8.7-p249@global");
//    Assert.assertEquals(envs.get("GEM_HOME"), rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity");
//    Assert.assertTrue(envs.get("PATH").startsWith(rvmHomePath + "/rubies/ruby-1.8.7-p249/bin:" +
//        rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity/bin:" +
//        rvmHomePath + "/gems/ruby-1.8.7-p249@global/bin:" +
//        rvmHomePath + "/bin"));
//    Assert.assertEquals(envs.get("MY_RUBY_HOME"), rvmHomePath + "/rubies/ruby-1.8.7-p249");
//    Assert.assertEquals(envs.get("BUNDLE_PATH"), rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity");
//    Assert.assertEquals(envs.get("rvm_ruby_string"), "ruby-1.8.7-p249");

    // successfully finished
    assertSuccessful(build);
  }

  @Test(groups = {"unix"})
  public void testRMVRCFilePathValidationOnAgent() throws Exception {
    if (!SystemInfo.isUnix) {
      throw new SkipException("Not a UNIX. RVM support is only for Unix. Set 'unix' group to ignore.");
    }

    assertSuccessful(runRVMRCBuild(""));
    assertSuccessful(runRVMRCBuild(".rvmrc"));
    assertSuccessful(runRVMRCBuild("some/path/.rvmrc"));
    assertFailed(runRVMRCBuild("invalid"));
  }

  @NotNull
  private SBuild runRVMRCBuild(@NotNull final String path) throws IOException {
    final HashMap<String, String> featureParamsMap = new HashMap<String, String>();

    // use rvm
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rvmrc");
    featureParamsMap.put(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY, path);
    getAgentEvents().addListener(new AgentLifeCycleAdapter() {
      @Override
      public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
        FileUtil.createIfDoesntExist(new File(runner.getBuild().getCheckoutDirectory(),
                                              StringUtil.isEmptyOrSpaces(path) ? ".rvmrc" : path));
      }
    });
    final SBuildType bt = configureFakeBuild(FakeBuildConfiguration.Feature, featureParamsMap);


    SBuild build = startBuild(bt, false);
    build = finishBuild(build);
    dumpBuildLog(build);
    return build;
  }

  private void assertFailed(@NotNull final SBuild build) {
    finishBuild(build); // Just to be sure
    Assert.assertTrue(build.getBuildStatus().isFailed(), "Build must be failed");
  }

  private void assertSuccessful(@NotNull final SBuild build) {
    finishBuild(build); // Just to be sure
    Assert.assertTrue(build.getBuildStatus().isSuccessful(), "Build must be successful");
  }


  //@Test
  //public void test_NonRvmSdk_EnvSettings() throws IOException {
  //  if (!SystemInfo.isUnix) {
  //    // TODO: use mocks
  //    // rmv support is only for Unix
  //    return;
  //  }
  //
  //  final Ref<Map<String, String>> envParamsRef = new Ref<Map<String, String>>();
  //  final Ref<Map<String, String>> sysPropertiesRef = new Ref<Map<String, String>>();
  //
  //  // add listener
  //  addBuildParamsListener(envParamsRef, sysPropertiesRef, null);
  //
  //  final SBuildType bt = configureFakeBuild(true, false);
  //
  //  final String interpreterPath =
  //    RakeRunnerTestUtil.getTestDataItemPath(".rvm/rubies/ruby-1.8.7-p249/bin/ruby").getAbsolutePath();
  //  addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RUBY_SDK_PATH_KEY, interpreterPath);
  //
  //  SBuild build = startBuild(bt, false);
  //  build = finishBuild(build);
  //  dumpBuildLog(build);
  //
  //  final Map<String, String> envs = envParamsRef.get();
  //  Assert.assertNotNull(envs);
  //  final String rvmHomePath = RVMPathsSettings.getInstance().getRvmHomePath();
  //  Assert.assertNull(envs.get("GEM_PATH"));
  //  Assert.assertNull("GEM_HOME");
  //  Assert.assertTrue(envs.get("PATH").startsWith(rvmHomePath + "/rubies/ruby-1.8.7-p249/bin:"));
  //  Assert.assertNull(envs.get("MY_RUBY_HOME"));
  //  Assert.assertNull(envs.get("BUNDLE_PATH"));
  //  Assert.assertNull(envs.get("rvm_ruby_string"));
  //
  //  // successfully finished
  //  Assert.assertTrue(build.getFailureReasons().isEmpty());
  //  Assert.assertTrue(build.getStatusDescriptor().isSuccessful());
  //}

  private SBuildType configureFakeBuild(@NotNull final FakeBuildConfiguration conf, @NotNull final Map<String, String> featureParamsMap)
    throws IOException {
    // create build
    final SBuildType bt = createBuildType();

    // remove predefined runners
    bt.removeAllBuildRunners();

    // add our fake runner
    bt.addBuildRunner("", RUN_TYPE, Collections.<String, String>emptyMap());

    switch (conf) {
      case Nothing:
        break;
      case FeatureAndFakeRvmHome: {
        final File testRvmHome = RakeRunnerTestUtil.getTestDataItemPath(".rvm");
        addBuildParameter(bt, "env.rvm_path", testRvmHome.getAbsolutePath());
      } // NO BREAK!
      case Feature: {
        bt.addBuildFeature(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE, featureParamsMap);
      }
    }
    return bt;
  }

  private void addBuildParamsListener(@Nullable final Ref<Map<String, String>> envParamsRef,
                                      @Nullable final Ref<Map<String, String>> sysPropertiesRef,
                                      @Nullable final Ref<Map<String, String>> allParamsRef,
                                      @Nullable final Ref<BuildRunnerContext> contextRef) {
    getAgentEvents().addListener(new AgentLifeCycleAdapter() {
      @Override
      public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
        if (contextRef != null) {
          contextRef.set(runner);
        }
      }

      @Override
      public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
        final BuildParametersMap buildParameters = runner.getBuildParameters();

        if (allParamsRef != null) {
          allParamsRef.set(new HashMap<String, String>(buildParameters.getAllParameters()));
        }
        if (envParamsRef != null) {
          envParamsRef.set(new HashMap<String, String>(buildParameters.getEnvironmentVariables()));
        }
        if (sysPropertiesRef != null) {
          sysPropertiesRef.set(new HashMap<String, String>(buildParameters.getSystemProperties()));
        }
      }
    });
  }

  private void addBuildParameter(@NotNull final BuildTypeSettings bt,
                                 @NotNull final String key,
                                 @NotNull final String value) {
    bt.addBuildParameter(new SimpleParameter(key, value));
  }
}


