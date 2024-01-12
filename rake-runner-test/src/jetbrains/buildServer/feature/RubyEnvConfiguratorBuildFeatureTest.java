

package jetbrains.buildServer.feature;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Vladislav.Rassokhin
 */
@Test(groups = "all")
public class RubyEnvConfiguratorBuildFeatureTest {

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

  @Test
  public void testResetPropertiesForRubyInterpreter() {
    Map<String, String> properties = createFullConfiguration();
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "");
    Collection<String> expectedProperties = Arrays.asList(
      RubyEnvConfiguratorConstants.UI_USE_RVM_KEY,
      RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY,
      RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY
    );
    checkReset(properties, expectedProperties);
  }

  @Test
  public void testResetPropertiesForRVMGemset() {
    Map<String, String> properties = createFullConfiguration();
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "manual");
    Collection<String> expectedProperties = Arrays.asList(
      RubyEnvConfiguratorConstants.UI_USE_RVM_KEY,
      RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY,
      RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY,
      RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY,
      RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS,
      RubyEnvConfiguratorConstants.UI_INNER_RVM_EXIST_REQUIREMENT_KEY
    );
    checkReset(properties, expectedProperties);
  }

  @Test
  public void testResetPropertiesForRVMRC() {
    Map<String, String> properties = createFullConfiguration();
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rvmrc");
    Collection<String> expectedProperties = Arrays.asList(
      RubyEnvConfiguratorConstants.UI_USE_RVM_KEY,
      RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY,
      RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY,
      RubyEnvConfiguratorConstants.UI_INNER_RVM_EXIST_REQUIREMENT_KEY
    );
    checkReset(properties, expectedProperties);
  }

  @Test
  public void testResetPropertiesForRVMDirectory() {
    Map<String, String> properties = createFullConfiguration();
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rvm_ruby_version");
    Collection<String> expectedProperties = Arrays.asList(
      RubyEnvConfiguratorConstants.UI_USE_RVM_KEY,
      RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY,
      RubyEnvConfiguratorConstants.UI_RVM_RUBY_VERSION_PATH_KEY,
      RubyEnvConfiguratorConstants.UI_INNER_RVM_EXIST_REQUIREMENT_KEY
    );
    checkReset(properties, expectedProperties);
  }

  @Test
  public void testResetPropertiesForRbenv() {
    Map<String, String> properties = createFullConfiguration();
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rbenv");
    Collection<String> expectedProperties = Arrays.asList(
      RubyEnvConfiguratorConstants.UI_USE_RVM_KEY,
      RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY,
      RubyEnvConfiguratorConstants.UI_RBENV_VERSION_NAME_KEY,
      RubyEnvConfiguratorConstants.UI_INNER_RBENV_EXIST_REQUIREMENT_KEY
    );
    checkReset(properties, expectedProperties);
  }

  @Test
  public void testResetPropertiesForRbenvVersionFile() {
    Map<String, String> properties = createFullConfiguration();
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "rbenv_file");
    Collection<String> expectedProperties = Arrays.asList(
      RubyEnvConfiguratorConstants.UI_USE_RVM_KEY,
      RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY,
      RubyEnvConfiguratorConstants.UI_RBENV_FILE_PATH_KEY,
      RubyEnvConfiguratorConstants.UI_INNER_RBENV_EXIST_REQUIREMENT_KEY
    );
    checkReset(properties, expectedProperties);
  }

  @Test
  public void testResetPropertiesToDefault() {
    Map<String, String> properties = createFullConfiguration();
    Map<String, String> defaults = new HashMap<String, String>() {{
      put(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY, "def_UI_RVM_RVMRC_PATH_KEY");
    }};
    properties.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "manual");
    createPropertiesProcessor(defaults).process(properties);
    assertEquals(properties.get(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY), "def_UI_RVM_RVMRC_PATH_KEY");
  }

  private boolean isRVMRCPathValid(@NotNull final String path) {
    return createPropertiesProcessor(Collections.emptyMap()).process(createRVMRCConfiguration(path)).isEmpty();
  }

  private boolean isRVMInterpreterNameValid(@Nullable final String name) {
    return createPropertiesProcessor(Collections.emptyMap()).process(createRVMConfiguration(name, null)).isEmpty();
  }

  private void checkReset(@NotNull Map<String, String> properties, @NotNull Collection<String> expectedProperties) {
    createPropertiesProcessor(Collections.emptyMap()).process(properties);
    assertEquals(properties.size(), expectedProperties.size());
    expectedProperties.forEach(expectedPropetry -> assertTrue(properties.containsKey(expectedPropetry)));
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

  private Map<String, String> createFullConfiguration() {
    return new HashMap<String, String>() {{
      put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, "UI_FAIL_BUILD_IF_NO_RUBY_FOUND");
      put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY, "UI_RVM_GEMSET_NAME");
      put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS, "UI_RVM_GEMSET_CREATE_IF_NON_EXISTS");
      put(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY, "UI_RVM_SDK_NAME");
      put(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY, "UI_RVM_RVMRC_PATH");
      put(RubyEnvConfiguratorConstants.UI_RVM_RUBY_VERSION_PATH_KEY, "UI_RVM_RUBY_VERSION_PATH");
      put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "UI_USE_RVM");
      put(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY, "UI_RUBY_SDK_PATH");
      put(RubyEnvConfiguratorConstants.UI_RBENV_VERSION_NAME_KEY, "UI_RBENV_VERSION_NAME");
      put(RubyEnvConfiguratorConstants.UI_RBENV_FILE_PATH_KEY, "UI_RBENV_FILE_PATH");
      put(RubyEnvConfiguratorConstants.UI_INNER_RVM_EXIST_REQUIREMENT_KEY, "UI_INNER_RVM_EXIST_REQUIREMENT");
      put(RubyEnvConfiguratorConstants.UI_INNER_RBENV_EXIST_REQUIREMENT_KEY, "UI_INNER_RBENV_EXIST_REQUIREMENT");
    }};
  }

  private PropertiesProcessor createPropertiesProcessor(@NotNull Map<String, String> defaultProperties) {
    return new RubyEnvConfiguratorBuildFeature.ParametersValidator(defaultProperties);
  }

}