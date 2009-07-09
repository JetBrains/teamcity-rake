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
import static jetbrains.slow.plugins.rakerunner.MockingOptions.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class RSpecMessagesTest extends AbstractRakeRunnerTest {
  public RSpecMessagesTest(String s) {
    super(s);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myShouldTranslateMessages = false;
  }

  protected void appendRunnerSpecificRunParameters(Map<String, String> runParameters) throws IOException, RunBuildException {
    super.appendRunnerSpecificRunParameters(runParameters);

    setWorkingDir(runParameters, "app_rspec");
    // enable rspec
    SupportedTestFramework.RSPEC.activate(runParameters);
  }

  public void testSpecOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("output:spec_output", false, "app_rspec");
  }

  public void testSpecPassed()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:passed", true, "app_rspec");
  }

  public void testSpecFailed()  throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_TIME, FAKE_STACK_TRACE, FAKE_LOCATION_URL);

    initAndDoTest("stat:failed", false, "app_rspec");
  }

  public void testSpecError()  throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_TIME, FAKE_STACK_TRACE, FAKE_LOCATION_URL);

    initAndDoTest("stat:error", false, "app_rspec");
  }

  public void testSpecIgnored()  throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:ignored", false, "app_rspec");
  }

  public void testSpecCompileError()  throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_TIME, FAKE_STACK_TRACE, FAKE_LOCATION_URL);
    initAndDoTest("stat:compile_error", false, "app_rspec");
  }

  public void testSpecLocation()  throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_TIME, FAKE_STACK_TRACE);
    initAndDoTest("stat:passed", "_location", true, "app_rspec");
  }
}
