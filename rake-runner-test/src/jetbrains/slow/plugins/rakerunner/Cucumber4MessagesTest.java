

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
public class Cucumber4MessagesTest extends AbstractCucumberTest {

  @Factory(dataProvider = "cucumber4", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public Cucumber4MessagesTest(@NotNull final String ruby, @NotNull final String cucumber) {
    super(ruby, cucumber);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return "app_cucumber-4";
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    super.beforeMethod2();
    setMessagesTranslationEnabled(false);
    activateTestFramework(SupportedTestFramework.CUCUMBER);
  }

  @Test
  public void testLocation() throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:features", "_location", true);
  }

  @Test
  public void testAmpresand() throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:features", "_ampersand", true);
  }
}