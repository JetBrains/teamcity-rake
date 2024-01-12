

package jetbrains.buildServer.runner.rakerunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.runner.BuildFileRunnerConstants;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Vladislav.Rassokhin
 */
@Test(groups = "all")
public class RakeRunnerRunTypeTest {

  public static final PropertiesProcessor PARAMETERS_PROCESSOR = new RakeRunnerRunType.ParametersValidator(Collections.emptyMap());

  @Test
  public void testRVMInterpreterNameValidation() throws Exception {
    assertTrue(isRVMInterpreterNameValid("ruby-1.9.3"));
    assertTrue(isRVMInterpreterNameValid("jruby"));
    assertTrue(isRVMInterpreterNameValid("%var%"));
    assertFalse(isRVMInterpreterNameValid(""));
    assertFalse(isRVMInterpreterNameValid(null));
  }

  @Test
  public void testResetRakefileContent() {
    Map<String, String> properties = new HashMap<String, String>() {{
      put(BuildFileRunnerConstants.BUILD_FILE_KEY, "build-file");
      put(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY, "build-file-path");
    }};
    PARAMETERS_PROCESSOR.process(properties);
    assertFalse(properties.containsKey(BuildFileRunnerConstants.BUILD_FILE_KEY));
    assertEquals(properties.get(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY), "build-file-path");
  }

  @Test
  public void testResetRakefile() {
    Map<String, String> properties = new HashMap<String, String>() {{
      put(BuildFileRunnerConstants.USE_CUSTOM_BUILD_FILE_KEY, "true");
      put(BuildFileRunnerConstants.BUILD_FILE_KEY, "build-file");
      put(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY, "build-file-path");
    }};
    PARAMETERS_PROCESSOR.process(properties);
    assertFalse(properties.containsKey(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY));
    assertEquals(properties.get(BuildFileRunnerConstants.BUILD_FILE_KEY), "build-file");
  }

  @Test
  public void testResetDefaultMode() {
    Map<String, String> properties = createFullModeConfiguration();
    properties.put(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE, RakeRunnerUtils.RubyConfigMode.DEFAULT.getModeValueString());
    PARAMETERS_PROCESSOR.process(properties);
    assertFalse(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH));
    assertFalse(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME));
    assertFalse(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME));
  }

  @Test
  public void testResetPathMode() {
    Map<String, String> properties = createFullModeConfiguration();
    properties.put(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE, RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH.getModeValueString());
    PARAMETERS_PROCESSOR.process(properties);
    assertTrue(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH));
    assertFalse(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME));
    assertFalse(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME));
  }

  @Test
  public void testResetRvmMode() {
    Map<String, String> properties = createFullModeConfiguration();
    properties.put(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE, RakeRunnerUtils.RubyConfigMode.RVM.getModeValueString());
    PARAMETERS_PROCESSOR.process(properties);
    assertFalse(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH));
    assertTrue(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME));
    assertTrue(properties.containsKey(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME));
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

  private Map<String, String> createFullModeConfiguration() {
    return new HashMap<String, String>() {{
      put(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH, "SERVER_UI_RUBY_INTERPRETER_PATH");
      put(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, "SERVER_UI_RUBY_RVM_SDK_NAME");
      put(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME, "SERVER_UI_RUBY_RVM_GEMSET_NAME");
    }};
  }
}