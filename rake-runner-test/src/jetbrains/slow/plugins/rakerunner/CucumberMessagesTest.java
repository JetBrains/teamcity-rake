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

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static jetbrains.slow.plugins.rakerunner.MockingOptions.FAKE_STACK_TRACE;
import static jetbrains.slow.plugins.rakerunner.MockingOptions.FAKE_TIME;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class CucumberMessagesTest extends AbstractRakeRunnerTest {
  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    activateTestFramework(SupportedTestFramework.CUCUMBER);
  }

  public void testLocation()  throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions(FAKE_TIME, FAKE_STACK_TRACE);
    initAndDoTest("stat:features", "_location", false, "app_cucumber");
  }
}