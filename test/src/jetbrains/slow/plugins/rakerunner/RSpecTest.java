package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.RunBuildException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class RSpecTest extends AbstractRakeRunnerTest {
  public RSpecTest(String s) {
    super(s);
  }

  protected void appendRunnerSpecificRunParameters(Map<String, String> runParameters) throws IOException, RunBuildException {
    setWorkingDir(runParameters, "app_rspec");
  }

  public void testSpecOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("output:spec_output", false, "app_rspec");
  }

  public void testSpecPassed()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:passed", true, "app_rspec");
  }

  public void testSpecFailed()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:failed", false, "app_rspec");
  }

  public void testSpecError()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:error", false, "app_rspec");
  }

  public void testSpecIgnored()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:ignored", false, "app_rspec");
  }

  public void testSpecCompileError()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:compile_error", false, "app_rspec");
  }

  //TODO - capturer
}
