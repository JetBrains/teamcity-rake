/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import jetbrains.buildServer.serverSide.SimpleParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.BeforeMethod;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractBundlerBasedRakeRunnerTest extends AbstractRakeRunnerTest {

  public static final String BUNDLE_INSTALL_TASK = "bundle:install";

  @BeforeMethod(dependsOnMethods = {"setUp1"})
  protected final void runBundleInstall() throws Throwable {
    getBuildType().addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY, Boolean.FALSE.toString()));
    try {
      initAndDoTest(BUNDLE_INSTALL_TASK, null, true, getTestDataApp());
    } finally {
      getBuildType().addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY, Boolean.TRUE.toString()));
    }
  }

  /**
   * Declared final because bug in TestNG.
   */
  @BeforeMethod
  @Override
  protected final void setUp1() throws Throwable {
    super.setUp1();
    setMessagesTranslationEnabled(true);
    useRVMGemSet(getRVMGemsetName());
    useBundleGemfile(getBundlerGemfileName());
    setUp2();
  }

  @NotNull
  protected abstract String getTestDataApp();

  @NotNull
  protected abstract String getBundlerGemfileName();

  @NotNull
  protected abstract String getRVMGemsetName();

  /**
   * Use instead of setUp1
   */
  protected void setUp2() throws Throwable {
  }

  protected void initAndDoTest(@NotNull final String taskName, final boolean shouldPass) throws Throwable {
    super.initAndDoTest(taskName, shouldPass, getTestDataApp());
  }

  protected void initAndDoTest(@NotNull final String taskName, @Nullable final String resultFileSuffix, final boolean shouldPass)
    throws Throwable {
    super.initAndDoTest(taskName, resultFileSuffix, shouldPass, getTestDataApp());
  }

  protected void initAndDoRealTest(@NotNull final String taskName, final boolean shouldPass) throws Throwable {
    initAndDoRealTest(taskName, shouldPass, getTestDataApp());
  }

  protected void doTestWithoutLogCheck(@NotNull final String task_full_name, final boolean shouldPass) throws Throwable {
    super.doTestWithoutLogCheck(task_full_name, shouldPass, getTestDataApp());
  }
}
