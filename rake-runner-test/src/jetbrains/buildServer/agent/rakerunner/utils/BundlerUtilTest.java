/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
