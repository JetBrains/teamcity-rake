/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class TestUnitBuildLogTest extends AbstractTestUnitTest {

  @Factory(dataProvider = "test-unit", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public TestUnitBuildLogTest(@NotNull final String ruby, @NotNull final String testunit) {
    super(ruby, testunit);
  }

  public void testTestPassed() throws Throwable {
    doTestWithoutLogCheck("stat:passed", true);
    assertTestsCount(4, 0, 0);
  }

  public void testTestFailed() throws Throwable {
    doTestWithoutLogCheck("stat:failed", false);
    assertTestsCount(0, 4, 0);
  }

  public void testTestError() throws Throwable {
    doTestWithoutLogCheck("stat:error", false);
    assertTestsCount(0, 2, 0);
  }

  public void testTestsOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("tests:test_output", false);
  }
}