

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class TestUnitBuildLogTest extends AbstractTestUnitTest {

  @Factory(dataProvider = "test-unit", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public TestUnitBuildLogTest(@NotNull final String ruby, @NotNull final String testunit) {
    super(ruby, testunit);
  }

  public void testTestPassed() throws Throwable {
    doTestWithoutLogCheck("stat:passed", true);
    assertTestsCount(4, 0, 0);
  }

  public void testTestFailed() throws Throwable {
    doTestWithoutLogCheck("stat:failed", false);
    assertTestsCount(0, 4, 0);
  }

  public void testTestError() throws Throwable {
    doTestWithoutLogCheck("stat:error", false);
    assertTestsCount(0, 2, 0);
  }

  public void testTestsOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("tests:test_output", false);
  }
}