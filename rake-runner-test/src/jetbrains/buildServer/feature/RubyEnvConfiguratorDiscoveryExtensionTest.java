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

package jetbrains.buildServer.feature;

import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RubyEnvConfiguratorDiscoveryExtensionTest extends BaseServerTestCase {

  private RubyEnvConfiguratorDiscoveryExtension myDiscovery;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myDiscovery = new RubyEnvConfiguratorDiscoveryExtension();
  }

  @Test
  public void test_simple_detection() throws Exception {
    myBuildType.addBuildRunner("1", RakeRunnerConstants.RUNNER_TYPE, Collections.<String, String>emptyMap());
    final List<DiscoveredObject> discovered = myDiscovery.discover(myBuildType);
    assertNotNull(discovered);
    assertEquals(2, discovered.size());
    for (DiscoveredObject object : discovered) {
      assertEquals(RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE, object.getType());
    }
  }
}
