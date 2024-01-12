

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 * @author Vladislav.Rassokhin
 */
@Test
public class TestUnitMessagesTest extends AbstractTestUnitTest {

  @Factory(dataProvider = "test-unit", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public TestUnitMessagesTest(@NotNull final String ruby, @NotNull final String testunit) {
    super(ruby, testunit);
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    super.beforeMethod2();
    setMessagesTranslationEnabled(false);
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
  }

  public void testTestsOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("tests:test_output", false);
  }

  public void testTestGeneral() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("stat:general", true);
  }

  public void testTestPassed() throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:passed", true);
  }

  public void testTestFailed() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("stat:failed", false);
  }

  public void testTestError() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("stat:error", false);
  }

  public void testTestCompileError() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("stat:compile_error", false);
  }

  public void testLocationUrl() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions();
    initAndDoTest("stat:passed", "_location", true);
  }
}