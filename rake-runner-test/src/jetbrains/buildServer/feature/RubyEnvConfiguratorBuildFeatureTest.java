package jetbrains.buildServer.feature;


import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Vladislav.Rassokhin
 */
@Test(groups = "all")
public class RubyEnvConfiguratorBuildFeatureTest {

  public static final PropertiesProcessor PARAMETERS_PROCESSOR = new RubyEnvConfiguratorBuildFeature.ParametersValidator();

  @Test
  public void testRVMRCFilePathValidation() throws Exception {
    assertTrue(isRVMRCPathValid(""));
    assertTrue(isRVMRCPathValid(".rvmrc"));
    assertTrue(isRVMRCPathValid("/.rvmrc"));
    assertTrue(isRVMRCPathValid("\\.rvmrc"));
    assertTrue(isRVMRCPathValid("some\\path/.rvmrc"));
    assertTrue(isRVMRCPathValid("some/path/.rvmrc"));
    assertTrue(isRVMRCPathValid("some\\path\\.rvmrc"));
    assertTrue(isRVMRCPathValid("%variable%"));
    assertFalse(isRVMRCPathValid("path/.rvmrc2"));
    assertFalse(isRVMRCPathValid("path\\.omnom"));
    assertFalse(isRVMRCPathValid(".rvmrc2"));
  }

  @Test
  public void testRVMInterpreterNameValidation() throws Exception {
    assertTrue(isRVMInterpreterNameValid("ruby-1.8.7"));
    assertTrue(isRVMInterpreterNameValid("jruby"));
    assertTrue(isRVMInterpreterNameValid("%variable%"));
    assertFalse(isRVMInterpreterNameValid(""));
    assertFalse(isRVMInterpreterNameValid(null));
  }

  private boolean isRVMRCPathValid(@NotNull final String path) {
    return PARAMETERS_PROCESSOR.process(createRVMRCConfiguration(path)).isEmpty();
  }

  private boolean isRVMInterpreterNameValid(@Nullable final String name) {
    return PARAMETERS_PROCESSOR.process(createRVMConfiguration(name, null)).isEmpty();
  }

  private Map<String, String> createRVMRCConfiguration(@NotNull final String path) {
    Map<String, String> map = new HashMap<String, String>();
    map.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rvmrc");
    map.put(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY, path);
    return map;
  }

  private Map<String, String> createRVMConfiguration(@Nullable final String name, @Nullable final String gemset) {
    Map<String, String> map = new HashMap<String, String>();
    map.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "manual");
    map.put(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY, name);
    map.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY, gemset);
    return map;
  }

}
