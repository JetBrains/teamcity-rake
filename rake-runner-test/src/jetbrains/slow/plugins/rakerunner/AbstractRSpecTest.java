

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractRSpecTest extends AbstractBundlerBasedRakeRunnerTest {

  public static final String APP_RSPEC = "app_rspec";

  protected AbstractRSpecTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    setMessagesTranslationEnabled(true);
    activateTestFramework(SupportedTestFramework.RSPEC);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return APP_RSPEC;
  }
}