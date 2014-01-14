/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractTestSpecTest extends AbstractBundlerBasedRakeRunnerTest {
  public static final String APP_TESTSPEC = "app_testspec";

  protected AbstractTestSpecTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  @NotNull
  @Override
  protected String getTestDataApp() {
    return APP_TESTSPEC;
  }

  @Override
  protected void beforeMethod2() throws Throwable {
    setMessagesTranslationEnabled(true);
    activateTestFramework(SupportedTestFramework.TEST_SPEC, SupportedTestFramework.TEST_UNIT);
  }
}
