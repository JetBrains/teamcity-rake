

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractTestSpecTest extends AbstractBundlerBasedRakeRunnerTest {
  public static final String APP_TESTSPEC = "app_testspec";

  protected AbstractTestSpecTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return APP_TESTSPEC;
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    setMessagesTranslationEnabled(true);
    activateTestFramework(SupportedTestFramework.TEST_SPEC, SupportedTestFramework.TEST_UNIT);
  }
}