

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 * @author Vladislav.Rassokhin
 */
@Test
public class ShouldaBuildLogTest extends AbstractShouldaTest {
  @Factory(dataProvider = "shoulda", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public ShouldaBuildLogTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    super.beforeMethod2();
    setMockingOptions(MockingOptions.FAKE_LOCATION_URL, MockingOptions.FAKE_STACK_TRACE);
  }

  public void testGeneral() throws Throwable {
    setPartialMessagesChecker();

    initAndDoRealTest("stat:general", false);
  }

  public void testCounts() throws Throwable {
    doTestWithoutLogCheck("stat:general", false);

    assertTestsCount(4, 2, 0);
  }
}