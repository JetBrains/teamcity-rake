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

import jetbrains.buildServer.agent.rakerunner.SupportedTestFramework;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
public class CucumberMessagesTest extends AbstractCucumberTest {

  @Factory(dataProvider = "cucumber", dataProviderClass = BundlerBasedTestsDataProvider.class)
  public CucumberMessagesTest(@NotNull final String ruby, @NotNull final String cucumber) {
    super(ruby, cucumber);
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    super.beforeMethod2();
    setMessagesTranslationEnabled(false);
    activateTestFramework(SupportedTestFramework.CUCUMBER);
  }

  @Test
  public void testLocation() throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:features", "_location", false);
  }

  @Test
  public void testAmpresand() throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:features", "_ampersand", false);
  }
}