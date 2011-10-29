/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class TestUnitMessagesTest extends AbstractRakeRunnerTest {
  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    setMessagesTranslationEnabled(false);
    activateTestFramework(SupportedTestFramework.TEST_UNIT);
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
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

  public void testLocationUrl()  throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions();
    initAndDoTest("stat:passed", "_location", true, "app_testunit");
  }
}
