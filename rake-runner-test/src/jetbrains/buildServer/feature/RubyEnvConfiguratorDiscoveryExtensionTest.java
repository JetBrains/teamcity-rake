

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