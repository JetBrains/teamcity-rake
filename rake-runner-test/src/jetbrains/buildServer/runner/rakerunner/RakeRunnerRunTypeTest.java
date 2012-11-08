package jetbrains.buildServer.runner.rakerunner;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Vladislav.Rassokhin
 */
@Test(groups = "all")
public class RakeRunnerRunTypeTest {

  public static final PropertiesProcessor PARAMETERS_PROCESSOR = new RakeRunnerRunType.ParametersValidator();

  @Test
  public void testRVMInterpreterNameValidation() throws Exception {
    assertTrue(isRVMInterpreterNameValid("ruby-1.9.3"));
    assertTrue(isRVMInterpreterNameValid("jruby"));
    assertTrue(isRVMInterpreterNameValid("%var%"));
    assertFalse(isRVMInterpreterNameValid(""));
    assertFalse(isRVMInterpreterNameValid(null));
  }

  private boolean isRVMInterpreterNameValid(@Nullable final String name) {
    return PARAMETERS_PROCESSOR.process(createRVMConfiguration(name)).isEmpty();
  }

  private Map<String, String> createRVMConfiguration(@Nullable final String name) {
    Map<String, String> map = new HashMap<String, String>();
    map.put(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE, RakeRunnerUtils.RubyConfigMode.RVM.getModeValueString());
    map.put(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, name);
    return map;
  }
}
