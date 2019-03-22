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

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test
public class RakeBuildScriptBuildLogTest extends AbstractRakeRunnerTest {
  private static final Logger LOG = Logger.getLogger(RakeBuildScriptBuildLogTest.class);
  @Factory(dataProviderClass = RubyVersionsDataProvider.class, dataProvider = "ruby-versions")
  public RakeBuildScriptBuildLogTest(@NotNull final String ruby) {
    setRubyVersion(ruby);
  }

  private boolean prepared;
  public void doPrepareEnvironment() throws Exception {
    if (prepared) return;
    try {
      if (!SystemInfo.isUnix) {
        return;
      }
      final File gemfile = getTestDataPath("gems/" + RakeRunnerTestUtil.DEFAULT_GEMSET_NAME + "/Gemfile");
      doPrepareGemset(getRubyVersion(), RakeRunnerTestUtil.DEFAULT_GEMSET_NAME, LOG, gemfile);
      FileUtil.delete(new File(gemfile.getParentFile(), "Gemfile.lock"));
    } finally {
      prepared = true;
    }
  }

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    doPrepareEnvironment();
    super.setUp1();
    setMessagesTranslationEnabled(true);
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
  }

  public void testBuildScript_stdout() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_out", true, "app1");
  }

  public void testBuildScript_stderr() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_err", true, "app1");
  }

  public void testBuildScript_show_one_task() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:show_one_task", true, "app1");
  }

  public void testBuildScript_show_one_task_trace() throws Throwable {
    rakeUI_EnableTraceOption();
    setPartialMessagesChecker();

    initAndDoTest("build_script:show_one_task", "_trace", true, "app1");
  }

  public void testBuildScript_exception_in_embedded_task_trace_real() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();

    initAndDoTest("build_script:exception_in_embedded_task", "_trace", false, "app1");
  }

  public void testBuildScript_warning_in_task_in_task() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:warning_in_task", true, "app1");
  }

  public void testBuildScript_compile_error_task() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("compile_error:some_task", false, "app2");
  }

  public void testBuildScript_embedded_tasks_trace() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();

    initAndDoTest("build_script:embedded_tasks", "_trace", true, "app1");
  }

  public void testBuildScript_cmd_failed_real() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();
    initAndDoRealTest("build_script:cmd_failed", false, "app1");
  }

  public void testBuildScript_depends_on_cmd_failed_real() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();
    initAndDoRealTest("build_script:depends_on_cmd_failed", false, "app1");
  }
}