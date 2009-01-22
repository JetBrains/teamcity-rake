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
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class TestUnitTest extends AbstractRakeRunnerTest {
  public TestUnitTest(String s) {
    super(s);
  }

  protected void appendRunnerSpecificRunParameters(Map<String, String> runParameters) throws IOException, RunBuildException {
    setWorkingDir(runParameters, "app_testunit");
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

  //TODO - capturer
}
