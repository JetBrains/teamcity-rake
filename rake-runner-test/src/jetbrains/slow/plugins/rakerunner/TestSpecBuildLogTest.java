

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class TestSpecBuildLogTest extends AbstractTestSpecTest {
  @Factory(dataProvider = "test-spec", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public TestSpecBuildLogTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  public void testGeneral() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(MockingOptions.FAKE_LOCATION_URL, MockingOptions.FAKE_STACK_TRACE);

    initAndDoRealTest("stat:general", false);
  }

  public void testCounts() throws Throwable {
    doTestWithoutLogCheck("stat:general", false);

    assertTestsCount(4, 2, 1);
  }
}