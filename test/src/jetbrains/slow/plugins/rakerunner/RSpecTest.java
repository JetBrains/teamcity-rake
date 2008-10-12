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

//  public void testBuildScript_stdout() throws Throwable {
//    setPartialMessagesChecker();
//
//    initAndDoTest("build_script:std_out", true, "app_rspec");
//  }

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
