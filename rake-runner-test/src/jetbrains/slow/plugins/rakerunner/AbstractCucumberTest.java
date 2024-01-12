

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman Chernyatchik
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractCucumberTest extends AbstractBundlerBasedRakeRunnerTest {
  protected AbstractCucumberTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @NotNull
  @Override
  abstract protected String getTestDataApp();

  @Override
  protected void beforeMethod2() throws Throwable {
    setMessagesTranslationEnabled(true);
    activateTestFramework(SupportedTestFramework.CUCUMBER);
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
  }
}