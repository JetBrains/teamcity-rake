package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.RunBuildException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class TestUnitTest extends AbstractRakeRunnerTest {
  public TestUnitTest(String s) {
    super(s);
  }

  protected void appendRunnerSpecificRunParameters(Map<String, String> runParameters) throws IOException, RunBuildException {
    setWorkingDir(runParameters, "app_testunit");
  }

  public void testTestsOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("tests:test_output", false, "app_testunit");
  }

  public void testTestSucessful() throws Throwable {
    setPartialMessagesChecker();
    
    initAndDoTest("stat:general", true, "app_testunit");

    assertSucessful();
  }

  public void testTestPassed()  throws Throwable {
    setNullMessageChecker();
    initAndDoTest("stat:general", true, "app_testunit");

    assertFailed();
  }

  public void testTestIgnored()  throws Throwable {
    setNullMessageChecker();
    initAndDoTest("stat:general", true, "app_testunit");

    // no ignored tests
    assertIgnored();
  }

  //TODO - setup/teardown
  //TODO - test std
  //TODO - test err
  //TODO - test pass
  //TODO - test failure
  //TODO - test error
  //TODO - test location info
  //TODO - test duration
  //TODO - test timestamp

  //TODO - suite empty
  //TODO - suite tests

  //TODO - test compilation error

  //TODO - capturer
}
