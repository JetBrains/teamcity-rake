

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class ShouldaMessagesTest extends AbstractShouldaTest {
  @Factory(dataProvider = "shoulda", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public ShouldaMessagesTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    super.beforeMethod2();
    setMessagesTranslationEnabled(false);
  }

  public void testLocation() throws Throwable {
    //TODO implement test location for shoulda!
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:general", "_location", false);
  }
}