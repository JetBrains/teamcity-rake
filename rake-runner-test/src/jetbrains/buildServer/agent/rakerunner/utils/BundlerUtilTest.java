

package jetbrains.buildServer.agent.rakerunner.utils;

import java.io.File;
import java.util.HashMap;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import jetbrains.buildServer.util.TestFor;
import jetbrains.slow.plugins.rakerunner.RakeRunnerTestUtil;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

public class BundlerUtilTest {
  protected File getTestDataPath(final String buildFileName) {
    return RakeRunnerTestUtil.getTestDataItemPath("bundler-discovery/" + buildFileName);
  }

  @Test
  @TestFor(issues = "TW-52173")
  public void testBundleExecutableFound() throws Exception {
    final HashMap<String, String> params = new HashMap<>();

    BundlerUtil.setBundleBinPath(params, getTestDataPath("bundler-1.0.22").getAbsolutePath());
    then(params).containsKey(BundlerUtil.BUNDLE_BIN_PATH_ENV_VAR);
    then(params.get(BundlerUtil.BUNDLE_BIN_PATH_ENV_VAR)).endsWith("bundler-1.0.22/bin/bundle".replace('/', File.separatorChar));
    params.clear();

    BundlerUtil.setBundleBinPath(params, getTestDataPath("bundler-1.11.2").getAbsolutePath());
    then(params).containsKey(BundlerUtil.BUNDLE_BIN_PATH_ENV_VAR);
    then(params.get(BundlerUtil.BUNDLE_BIN_PATH_ENV_VAR)).endsWith("bundler-1.11.2/exe/bundle".replace('/', File.separatorChar));
    params.clear();

    try {
      BundlerUtil.setBundleBinPath(params, getTestDataPath("bundler-weird").getAbsolutePath());
      fail("Exception expected");
    } catch (RakeTasksBuildService.MyBuildFailureException e) {
    }
    params.clear();
  }


}