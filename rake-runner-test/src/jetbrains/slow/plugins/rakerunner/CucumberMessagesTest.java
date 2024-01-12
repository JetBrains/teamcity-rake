

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
public class CucumberMessagesTest extends AbstractCucumberTest {

  @Factory(dataProvider = "cucumber", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public CucumberMessagesTest(@NotNull final String ruby, @NotNull final String cucumber) {
    super(ruby, cucumber);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return "app_cucumber";
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
    initAndDoTest("stat:features", "_location", false);
  }

  @Test
  public void testAmpresand() throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:features", "_ampersand", false);
  }
}