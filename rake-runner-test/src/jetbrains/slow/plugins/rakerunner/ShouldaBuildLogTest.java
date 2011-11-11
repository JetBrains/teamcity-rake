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
@Test(groups = {"all", "slow"})
public class ShouldaBuildLogTest extends AbstractRakeRunnerTest {

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    setMessagesTranslationEnabled(true);
    activateTestFramework(SupportedTestFramework.SHOULDA);
    setMockingOptions(MockingOptions.FAKE_LOCATION_URL, MockingOptions.FAKE_STACK_TRACE);
    useRVMGemSet("shoulda-trunk");
  }

  public void testGeneral() throws Throwable {
    setPartialMessagesChecker();

    initAndDoRealTest("stat:general", false, "app_shoulda");
  }

  public void testCounts() throws Throwable {
    doTestWithoutLogCheck("stat:general", false, "app_shoulda");

    assertTestsCount(4, 2, 0);
  }
}