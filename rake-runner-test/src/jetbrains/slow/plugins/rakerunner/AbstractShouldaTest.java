

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman Chernyatchik
 */
public abstract class AbstractShouldaTest extends AbstractBundlerBasedRakeRunnerTest {

  public static final String APP_SHOULDA = "app_shoulda";

  protected AbstractShouldaTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return APP_SHOULDA;
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    activateTestFramework(SupportedTestFramework.SHOULDA, SupportedTestFramework.TEST_UNIT);
  }
}