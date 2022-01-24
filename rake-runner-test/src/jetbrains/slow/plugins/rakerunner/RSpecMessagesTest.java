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

import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class RSpecMessagesTest extends AbstractRSpecTest {
  @Factory(dataProvider = "rspec", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public RSpecMessagesTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    super.beforeMethod2();
    setMessagesTranslationEnabled(false);
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
  }

  public void testSpecOutput() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("output:spec_output", false);
  }

  @TestFor(issues = "TW-38596")
  public void testSpecSeedOutput() throws Throwable {
    setPartialMessagesChecker();

    addRunParameter(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY, "--seed 38596");
    initAndDoTest("output:spec_output", "_spec", false);
  }

  public void testSpecPassed() throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:passed", true);
  }

  public void testSpecFailed() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("stat:failed", false);
  }

  public void testSpecError() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("stat:error", false);
  }

  public void testSpecIgnored() throws Throwable {
    setPartialMessagesChecker();
    if (getRVMGemsetName().equals("rspec-2")) {
      initAndDoTest("stat:ignored", ".rspec-2", false);
    } else {
      initAndDoTest("stat:ignored", false);
    }
  }

  public void testSpecCompileError() throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("stat:compile_error", false);
  }

  public void testSpecLocation() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions();
    initAndDoTest("stat:passed", "_location", true);
  }
}
