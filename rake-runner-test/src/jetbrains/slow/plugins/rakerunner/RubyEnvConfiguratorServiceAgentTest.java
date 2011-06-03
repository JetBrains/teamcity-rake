package jetbrains.slow.plugins.rakerunner;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.feature.RubyEnvConfiguratorService;
import jetbrains.buildServer.agent.impl.SpringContextPerAgentXml;
import jetbrains.buildServer.agent.rakerunner.RubyLightweightSdk;
import jetbrains.buildServer.feature.RubyEnvConfiguratorUtil;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.impl.AgentServerFunctionalTestCase;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static jetbrains.buildServer.agent.rakerunner.SharedRubyEnvSettings.*;

/**
 * @author Roman.Chernyatchik
 */
@TestFor(testForClass = {RubyEnvConfiguratorService.class})
@SpringContextPerAgentXml(configs = {"classpath*:/META-INF/build-agent-plugin-rakerunner.xml"})
public class RubyEnvConfiguratorServiceAgentTest extends AgentServerFunctionalTestCase {
  private final String RUN_TYPE = "mySomeRunner!";

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();

    getAgentEvents().addListener(new AgentLifeCycleAdapter(){
      @Override
      public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
        runner.getBuild().getBuildLogger().message("message");
      }
    });

    createNonBlockingBuildRunner(RUN_TYPE);
  }

  @Test
  public void test_cmdlinePatcher_enabled() throws IOException {
    final AtomicBoolean patcherEnabled = new AtomicBoolean(false);

    getExtensionHolder().registerExtension(BuildRunnerPrecondition.class, "aaa",
                                           new RubyEnvConfiguratorService() {
                                             @Override
                                             protected void patchRunnerEnvironment(@NotNull final BuildRunnerContext context,
                                                                                   @NotNull final RubyLightweightSdk sdk)
                                               throws RunBuildException {

                                               patcherEnabled.set(true);
                                             }
                                           });

    // configure build
    final SBuildType bt = configureFakeBuild(true, false);

    // enable ruby env build feature
    enableRubyEnvBuildFeature(bt);

    // launch buld
    finishBuild(startBuild(bt, false));

    assertTrue("RubyEnvConfiguratorService should be enabled", patcherEnabled.get());
  }

  @Test
  public void test_cmdlinePatcher_disabled() throws IOException {
    final AtomicBoolean patcherEnabled = new AtomicBoolean(false);

    getExtensionHolder().registerExtension(BuildRunnerPrecondition.class, "aaa",
                                           new RubyEnvConfiguratorService() {
                                             @Override
                                             protected void patchRunnerEnvironment(@NotNull final BuildRunnerContext context,
                                                                                   @NotNull final RubyLightweightSdk sdk)
                                               throws RunBuildException {

                                               patcherEnabled.set(true);
                                             }
                                           });

    // configure build
    final SBuildType bt = configureFakeBuild(false, false);

    // don't enable ruby env build feature

    // launch buld
    finishBuild(startBuild(bt, false));

    assertFalse("RubyEnvConfiguratorService should not be enabled", patcherEnabled.get());
  }


  @Test
  public void test_setSharedOptionsRvm() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, contextRef);

    final SBuildType bt = configureFakeBuild(true, true);
    // use rvm
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_USE_RVM_KEY, "true");
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RVM_SDK_NAME_KEY, "ruby-1.8.7-p249");
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RVM_GEMSET_NAME_KEY, "teamcity");

    // launch
    finishBuild(startBuild(bt, false));

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    Assert.assertEquals(params.get(SHARED_RUBY_PARAMS_ARE_SET), "true");
    Assert.assertEquals(params.get(SHARED_RUBY_PARAMS_ARE_APPLIED), "true");
    Assert.assertEquals(params.get(SHARED_RUBY_RVM_SDK_NAME), "ruby-1.8.7-p249");
    Assert.assertEquals(params.get(SHARED_RUBY_RVM_GEMSET_NAME), "teamcity");
    Assert.assertNull(params.get(SHARED_RUBY_INTERPRETER_PATH));
  }

  @Test
  public void test_setSharedOptionsNoRvm() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, contextRef);

    final SBuildType bt = configureFakeBuild(true, false);

    final String interpreterPath =
      RakeRunnerTestUtil.getTestDataItemPath(".rvm/rubies/ruby-1.8.7-p249/bin/ruby").getAbsolutePath();
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RUBY_SDK_PATH_KEY, interpreterPath);

    // launch
    try {
      finishBuild(startBuild(bt, false));
    } catch (Exception e) {
      // it is ok, our fake ruby interpreter isn't executable
    }

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    Assert.assertEquals(params.get(SHARED_RUBY_PARAMS_ARE_SET), "true");
    Assert.assertEquals(params.get(SHARED_RUBY_PARAMS_ARE_APPLIED), "true");
    Assert.assertEquals(params.get(SHARED_RUBY_INTERPRETER_PATH), interpreterPath);
    Assert.assertNull(params.get(SHARED_RUBY_RVM_SDK_NAME));
    Assert.assertNull(params.get(SHARED_RUBY_RVM_GEMSET_NAME));
  }

  @Test
  public void test_BadRvmSdk_DoesntFailBuild() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, contextRef);

    final SBuildType bt = configureFakeBuild(true, false);

    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RUBY_SDK_PATH_KEY, "this path doesn't exist");
    // default : RubyEnvConfiguratorUtil.UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY = "false"

    // launch
    final SFinishedBuild build = finishBuild(startBuild(bt, false));

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    Assert.assertEquals(params.get(SHARED_RUBY_PARAMS_ARE_SET), "true");
    Assert.assertNull(params.get(SHARED_RUBY_PARAMS_ARE_APPLIED));

    Assert.assertTrue(build.getBuildProblems().isEmpty());
    Assert.assertTrue(build.getStatusDescriptor().isSuccessful());
  }

  @Test
  public void test_BadRvmSdk_FailsBuild() throws IOException {
    final Ref<BuildRunnerContext> contextRef = new Ref<BuildRunnerContext>();

    // add listener
    addBuildParamsListener(null, null, contextRef);

    final SBuildType bt = configureFakeBuild(true, false);

    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RUBY_SDK_PATH_KEY, "this path doesn't exist");
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY, "true");

    // launch
    final SFinishedBuild build = finishBuild(startBuild(bt, false));

    // check shared params:
    final Map<String, String> params = contextRef.get().getRunnerParameters();
    Assert.assertNotNull(params);

    Assert.assertEquals(params.get(SHARED_RUBY_PARAMS_ARE_SET), "true");
    Assert.assertNull(params.get(SHARED_RUBY_PARAMS_ARE_APPLIED));

    Assert.assertFalse(build.getBuildProblems().isEmpty());
    Assert.assertFalse(build.getStatusDescriptor().isSuccessful());
  }

  @Test
  public void test_RvmSdk_EnvSettings() throws IOException {
    if (!SystemInfo.isUnix) {
      // TODO: use mocks
      // rmv support is only for Unix
      return;
    }

    final Ref<Map<String, String>> envParamsRef = new Ref<Map<String, String>>();
    final Ref<Map<String, String>> sysPropertiesRef = new Ref<Map<String, String>>();

    // add listener
    addBuildParamsListener(envParamsRef, sysPropertiesRef, null);

    final SBuildType bt = configureFakeBuild(true, true);

    // use rvm
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_USE_RVM_KEY, "true");
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RVM_SDK_NAME_KEY, "ruby-1.8.7-p249");
    addBuildParameter(bt, RubyEnvConfiguratorUtil.UI_RVM_GEMSET_NAME_KEY, "teamcity");

    SBuild build = startBuild(bt, false);
    build = finishBuild(build);
    dumpBuildLog(build);

    final Map<String, String> envs = envParamsRef.get();
    Assert.assertNotNull(envs);
    final String rvmHomePath = RVMPathsSettings.getInstance().getRvmHomePath();
    Assert.assertEquals(envs.get("GEM_PATH"), rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity:"
                                              + rvmHomePath + "/gems/ruby-1.8.7-p249@global");
    Assert.assertEquals(envs.get("GEM_HOME"), rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity");
    Assert.assertTrue(envs.get("PATH").startsWith(rvmHomePath + "/rubies/ruby-1.8.7-p249/bin:" +
                                                  rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity/bin:" +
                                                  rvmHomePath + "/gems/ruby-1.8.7-p249@global/bin:" +
                                                  rvmHomePath + "/bin"));
    Assert.assertEquals(envs.get("MY_RUBY_HOME"), rvmHomePath + "/rubies/ruby-1.8.7-p249");
    Assert.assertEquals(envs.get("BUNDLE_PATH"), rvmHomePath + "/gems/ruby-1.8.7-p249@teamcity");
    Assert.assertEquals(envs.get("rvm_ruby_string"), "ruby-1.8.7-p249");

    // sucessfully finished
    Assert.assertTrue(build.getBuildProblems().isEmpty());
    Assert.assertTrue(build.getStatusDescriptor().isSuccessful());
  }


  @Test
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
  //  // sucessfully finished
  //  Assert.assertTrue(build.getBuildProblems().isEmpty());
  //  Assert.assertTrue(build.getStatusDescriptor().isSuccessful());
  //}

  private SBuildType configureFakeBuild(final boolean enableBuildFeature,
                                        final boolean enableFakeRvm) throws IOException {
    // create build
    final SBuildType bt = createBuildType();

    // remove predefined runners
    bt.removeAllBuildRunners();

    // add our fake runner
    bt.addBuildRunner("", RUN_TYPE, Collections.<String, String>emptyMap());

    if (enableBuildFeature) {
      enableRubyEnvBuildFeature(bt);

      // fake rvm home
      if (enableFakeRvm) {
        final File testRvmHome = RakeRunnerTestUtil.getTestDataItemPath(".rvm");
        addBuildParameter(bt, "env.rvm_path", testRvmHome.getAbsolutePath());
      }
    }
    return bt;
  }

  private void addBuildParamsListener(@Nullable final Ref<Map<String, String>> envParamsRef,
                                      @Nullable final Ref<Map<String, String>> sysPropertiesRef,
                                      @Nullable final Ref<BuildRunnerContext> contextRef) {
    getAgentEvents().addListener(new AgentLifeCycleAdapter(){
      @Override
      public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
        if (contextRef != null) {
          contextRef.set(runner);
        }
      }

      @Override
      public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
        final BuildParametersMap buildParameters = runner.getBuildParameters();

        if (envParamsRef != null) {
          envParamsRef.set(new HashMap<String, String>(buildParameters.getEnvironmentVariables()));
        }
        if (sysPropertiesRef != null) {
          sysPropertiesRef.set(new HashMap<String, String>(buildParameters.getSystemProperties()));
        }
      }
    });
  }

  private void enableRubyEnvBuildFeature(@NotNull final BuildTypeSettings bt) {
    addBuildParameter(bt, RubyEnvConfiguratorUtil.RUBY_ENV_CONFIGURATOR_KEY, "true");
  }

  private void addBuildParameter(@NotNull final BuildTypeSettings bt,
                                 final String key, final String value) {
    bt.addBuildParameter(new SimpleParameter(key, value));
  }

  // TODO- UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY
}


