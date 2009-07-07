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

import java.io.IOException;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class TestUnitBuildLogTest extends AbstractRakeRunnerTest {
  public TestUnitBuildLogTest(String s) {
    super(s);
  }

  protected void appendRunnerSpecificRunParameters(Map<String, String> runParameters) throws IOException, RunBuildException {
    setWorkingDir(runParameters, "app_testunit");

    // enable Test::Unit
    SupportedTestFramework.TEST_UNIT.activate(runParameters);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myShouldTranslateMessages = true;
  }

  public void testTestPassed()  throws Throwable {
    doTestWithoutLogCheck("stat:passed", true, "app_testunit");
    assertTestsCount(4, 0, 0);
  }

  public void testTestFailed()  throws Throwable {
    doTestWithoutLogCheck("stat:failed", false, "app_testunit");
    assertTestsCount(0, 4, 0);
  }

  public void testTestError()  throws Throwable {
    doTestWithoutLogCheck("stat:error", false, "app_testunit");
    assertTestsCount(0, 2, 0);
  }

  public void testTestsOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("tests:test_output", false, "app_testunit");
  }
}