

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.serverSide.BuildStatistics;
import jetbrains.buildServer.serverSide.BuildStatisticsOptions;
import jetbrains.buildServer.serverSide.SBuild;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
public class Cucumber4BuildLogTest extends AbstractCucumberTest {

  @Factory(dataProvider = "cucumber4", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public Cucumber4BuildLogTest(@NotNull final String ruby, @NotNull final String cucumber) {
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
    setMessagesTranslationEnabled(true);
    activateTestFramework(SupportedTestFramework.CUCUMBER);
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
    setBuildEnvironmentVariable("CUCUMBER_PUBLISH_QUIET", "true");
  }

  @Test
  public void testGeneral() throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:features", true);
  }

  @Test
  public void testCounts() throws Throwable {
    doTestWithoutLogCheck("stat:features", true);

    final SBuild build = getLastFinishedBuild();

    final BuildStatistics statNotGrouped = build.getBuildStatistics(
      new BuildStatisticsOptions(BuildStatisticsOptions.PASSED_TESTS | BuildStatisticsOptions.IGNORED_TESTS | BuildStatisticsOptions.NO_GROUPING_BY_NAME, 0));

    assertTestsCount(6, 0, 0, statNotGrouped);
    assertTestsCount(6, 0, 0, build.getFullStatistics());
    assertTestsCount(6, 0, 0, build.getShortStatistics());
  }
}