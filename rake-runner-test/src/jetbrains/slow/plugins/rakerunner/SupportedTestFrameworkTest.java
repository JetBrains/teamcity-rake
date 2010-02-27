/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.slow.plugins.rakerunner;

import java.util.HashMap;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class SupportedTestFrameworkTest extends BaseTestCase {
  private HashMap<String,String> myDefaultParams;

  @Override
  @BeforeMethod
  protected void setUp() throws Exception {
    super.setUp();

    myDefaultParams = new HashMap<String, String>();
    myDefaultParams.put(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY,
                        RakeRunnerConstants.CURRENT_CONFIG_VERSION);
  }

  public void testFrameworksCount() {
    assertEquals(5, SupportedTestFramework.values().length);
  }

  public void testFrameworksRubyIds() {
    assertEquals(":test_unit", SupportedTestFramework.TEST_UNIT.getFrameworkId());
    assertEquals(":test_spec", SupportedTestFramework.TEST_SPEC.getFrameworkId());
    assertEquals(":shoulda", SupportedTestFramework.SHOULDA.getFrameworkId());
    assertEquals(":rspec", SupportedTestFramework.RSPEC.getFrameworkId());
    assertEquals(":cucumber", SupportedTestFramework.CUCUMBER.getFrameworkId());
  }

  public void testIsActivated_SmthEnabled() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isAnyFrameworkActivated(myDefaultParams));
  }

  public void testIsActivated_SmthDisabled() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                        Boolean.FALSE.toString());
    assertFalse(SupportedTestFramework.isAnyFrameworkActivated(myDefaultParams));
  }

  public void testIsActivated_Empty() {
    assertFalse(SupportedTestFramework.isAnyFrameworkActivated(myDefaultParams));
  }

  public void testIsTestUnitActivated_TestUnit() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(myDefaultParams));
  }

  public void testIsTestUnitActivated_TestUnitAndSomeBdd() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(myDefaultParams));
  }

  public void testIsTestUnitActivated_TestSpec() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(myDefaultParams));
  }

  public void testIsTestUnitActivated_Shoulda() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_SHOULDA_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(myDefaultParams));
  }

  public void testIsTestUnitActivated_RSpecCucumber() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    assertFalse(SupportedTestFramework.isTestUnitBasedFrameworksActivated(myDefaultParams));
  }
  public void testIsActivated() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY,
                        Boolean.FALSE.toString());

    assertTrue(SupportedTestFramework.RSPEC.isActivated(myDefaultParams));
    assertTrue(SupportedTestFramework.CUCUMBER.isActivated(myDefaultParams));
    assertTrue(SupportedTestFramework.TEST_UNIT.isActivated(myDefaultParams));

    assertFalse(SupportedTestFramework.TEST_SPEC.isActivated(myDefaultParams));
    assertFalse(SupportedTestFramework.SHOULDA.isActivated(myDefaultParams));
  }

  public void testGetActivatedFrameworksConfig_Empty() {
    assertEquals("", SupportedTestFramework.getActivatedFrameworksConfig(myDefaultParams));
  }

  public void testGetActivatedFrameworksConfig_OneFramework() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());

    assertEquals(":rspec ", SupportedTestFramework.getActivatedFrameworksConfig(myDefaultParams));
  }

  public void testGetActivatedFrameworksConfig_SeveralFrameworks() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                        Boolean.TRUE.toString());

    assertEquals(":test_unit :rspec ", SupportedTestFramework.getActivatedFrameworksConfig(myDefaultParams));
  }

  public void testGetActivatedFrameworksConfig_DisabledFramework() {
    myDefaultParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                        Boolean.FALSE.toString());

    assertEquals("", SupportedTestFramework.getActivatedFrameworksConfig(myDefaultParams));
  }

  public void testConvertOptionsIfNecessary_LatestVersion() {
    SupportedTestFramework.convertOptionsIfNecessary(myDefaultParams);
    assertFalse(SupportedTestFramework.isAnyFrameworkActivated(myDefaultParams));
  }

  public void testConvertOptionsIfNecessary_NoVersion() {
    final HashMap<String, String> params = new HashMap<String, String>();
    SupportedTestFramework.convertOptionsIfNecessary(params);
    assertEquals(":test_unit :rspec ", SupportedTestFramework.getActivatedFrameworksConfig(params));
  }

  public void testConvertOptionsIfNecessary_Version1() {
    final HashMap<String, String> params = new HashMap<String, String>();
    SupportedTestFramework.convertOptionsIfNecessary(params);
    assertEquals(":test_unit :rspec ", SupportedTestFramework.getActivatedFrameworksConfig(params));
  }
}
