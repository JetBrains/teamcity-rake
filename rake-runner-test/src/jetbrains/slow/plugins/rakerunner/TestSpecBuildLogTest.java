/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
public class TestSpecBuildLogTest extends AbstractTestSpecTest {
  @Factory(dataProvider = "test-spec", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public TestSpecBuildLogTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  public void testGeneral() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(MockingOptions.FAKE_LOCATION_URL, MockingOptions.FAKE_STACK_TRACE);

    initAndDoRealTest("stat:general", false);
  }

  public void testCounts() throws Throwable {
    doTestWithoutLogCheck("stat:general", false);

    assertTestsCount(4, 2, 1);
  }
}