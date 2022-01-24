/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
 * @author Vladislav.Rassokhin
 */
@Test
public class RSpecBuildLogTest extends AbstractRSpecTest {
  @Factory(dataProvider = "rspec", dataProviderClass = BundlerBasedTestsDataProvider.class)
  @TestWithGemfiles("rspec-trunk")
  public RSpecBuildLogTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  public void testSpecPassed() throws Throwable {
    doTestWithoutLogCheck("stat:passed", true);

    assertTestsCount(3, 0, 0);
  }

  public void testSpecFailed() throws Throwable {
    doTestWithoutLogCheck("stat:failed", false);

    assertTestsCount(0, 3, 0);
  }

  public void testSpecError() throws Throwable {
    doTestWithoutLogCheck("stat:error", false);

    assertTestsCount(0, 3, 0);
  }

  public void testSpecIgnored() throws Throwable {
    doTestWithoutLogCheck("stat:ignored", false);
    if (getRVMGemsetName().equals("rspec-2")) {
      assertTestsCount(0, 1, 2);
    } else {
      assertTestsCount(0, 1, 8);
    }
  }

  public void testSpecCompileError() throws Throwable {
    doTestWithoutLogCheck("stat:compile_error", false);

    assertTestsCount(0, 0, 0);
  }
}