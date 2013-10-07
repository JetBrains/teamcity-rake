/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.runner.rakerunner;

import com.intellij.util.containers.ArrayListSet;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.runner.BuildFileRunnerConstants;
import jetbrains.buildServer.serverSide.DiscoveredBuildRunner;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.FileSystemBrowser;
import jetbrains.slow.plugins.rakerunner.RakeRunnerTestUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

public class RakeRunnerDiscoveryExtensionTest extends BaseTestCase {

  private RakeRunnerDiscoveryExtension myRakeRunnerDiscovery;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myRakeRunnerDiscovery = new RakeRunnerDiscoveryExtension();
  }

  @Test
  public void test_rakefile_detected() throws Exception {
    final File root = RakeRunnerTestUtil.getTestDataItemPath("discovery/nur-rake");
    final FileSystemBrowser browser = new FileSystemBrowser(root);
    final List<DiscoveredBuildRunner> discovered = myRakeRunnerDiscovery.discover(browser);
    assertNotNull(discovered);
    assertEquals(1, discovered.size());

    assertEquals("Rakefile", discovered.get(0).getParameters().get(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY));
    assertEquals(Boolean.FALSE, Boolean.valueOf(discovered.get(0).getParameters().get(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY)));
  }

  @Test
  public void test_two_rakefiles_detected() throws Exception {
    final File root = RakeRunnerTestUtil.getTestDataItemPath("discovery/rake-and-rake");
    final FileSystemBrowser browser = new FileSystemBrowser(root);
    final List<DiscoveredBuildRunner> discovered = myRakeRunnerDiscovery.discover(browser);
    assertNotNull(discovered);
    assertEquals(2, discovered.size());

    final Set<String> rakefiles = new ArrayListSet<String>();
    for (DiscoveredBuildRunner runner : discovered) {
      rakefiles.add(runner.getParameters().get(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY));
    }
    assertEquals(2, rakefiles.size());
    assertEquals(CollectionsUtil.setOf("Rakefile", "rakefile.rb"), rakefiles);
  }

  @Test
  public void test_rakefile_and_gemfile_detected() throws Exception {
    final File root = RakeRunnerTestUtil.getTestDataItemPath("discovery/rake-and-bundler");
    final FileSystemBrowser browser = new FileSystemBrowser(root);
    final List<DiscoveredBuildRunner> discovered = myRakeRunnerDiscovery.discover(browser);
    assertNotNull(discovered);
    assertEquals(1, discovered.size());

    final Map<String,String> parameters = discovered.get(0).getParameters();
    assertEquals("Rakefile", parameters.get(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY));
    assertEquals("true", parameters.get(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY));
    assertEquals(Boolean.TRUE, Boolean.valueOf(parameters.get(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY)));
  }
}
