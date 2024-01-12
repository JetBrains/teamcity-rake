

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class TestSpecMessagesTest extends AbstractTestSpecTest {
  @Factory(dataProvider = "test-spec", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public TestSpecMessagesTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  public void testLocation() throws Throwable {
    //TODO implement test location for test-spec!
    setMessagesTranslationEnabled(false);
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:general", "_location", false);
  }
}