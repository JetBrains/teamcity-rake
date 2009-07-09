/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class SupportedTestFrameworkTest extends TestCase {
  
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
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isAnyFrameworkActivated(runParams));
  }

  public void testIsActivated_SmthDisabled() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                  Boolean.FALSE.toString());
    assertFalse(SupportedTestFramework.isAnyFrameworkActivated(runParams));
  }

  public void testIsActivated_Empty() {
    assertFalse(SupportedTestFramework.isAnyFrameworkActivated(new HashMap<String, String>()));
  }

  public void testIsTestUnitActivated_TestUnit() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams));
  }

  public void testIsTestUnitActivated_TestUnitAndSomeBdd() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams));
  }

  public void testIsTestUnitActivated_TestSpec() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams));
  }

  public void testIsTestUnitActivated_Shoulda() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_SHOULDA_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    assertTrue(SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams));
  }

  public void testIsTestUnitActivated_RSpecCucumber() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    assertFalse(SupportedTestFramework.isTestUnitBasedFrameworksActivated(runParams));
  }
  public void testIsActivated() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY,
                  Boolean.FALSE.toString());

    assertTrue(SupportedTestFramework.RSPEC.isActivated(runParams));
    assertTrue(SupportedTestFramework.CUCUMBER.isActivated(runParams));
    assertTrue(SupportedTestFramework.TEST_UNIT.isActivated(runParams));

    assertFalse(SupportedTestFramework.TEST_SPEC.isActivated(runParams));
    assertFalse(SupportedTestFramework.SHOULDA.isActivated(runParams));
  }

  public void testGetActivatedFrameworksConfig_Empty() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    assertEquals("", SupportedTestFramework.getActivatedFrameworksConfig(runParams));
  }

  public void testGetActivatedFrameworksConfig_OneFramework() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());

    assertEquals(":rspec ", SupportedTestFramework.getActivatedFrameworksConfig(runParams));
  }

  public void testGetActivatedFrameworksConfig_SeveralFrameworks() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());
    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY,
                  Boolean.TRUE.toString());

    assertEquals(":test_unit :rspec ", SupportedTestFramework.getActivatedFrameworksConfig(runParams));
  }

  public void testGetActivatedFrameworksConfig_DisabledFramework() {
    final HashMap<String, String> runParams = new HashMap<String, String>();

    runParams.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY,
                  Boolean.FALSE.toString());

    assertEquals("", SupportedTestFramework.getActivatedFrameworksConfig(runParams));
  }
}
