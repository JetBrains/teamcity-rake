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

  public void testTestGeneral() throws Throwable {
    setPartialMessagesChecker();
    
    initAndDoTest("stat:general", true, "app_testunit");
  }

  public void testTestPassed()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:passed", true, "app_testunit");
  }

  public void testTestFailed()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:failed", false, "app_testunit");
  }

  public void testTestError()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:error", false, "app_testunit");
  }

  public void testTestCompileError()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:compile_error", false, "app_testunit");
  }

  //TODO - capturer
}
