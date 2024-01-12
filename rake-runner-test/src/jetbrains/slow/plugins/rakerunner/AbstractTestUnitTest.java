

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractTestUnitTest extends AbstractBundlerBasedRakeRunnerTest {
  public static final String APP_TESTUNIT = "app_testunit";

  protected AbstractTestUnitTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return APP_TESTUNIT;
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    activateTestFramework(SupportedTestFramework.TEST_UNIT);
  }
}