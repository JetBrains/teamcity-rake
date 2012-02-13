/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static jetbrains.slow.plugins.rakerunner.MockingOptions.*;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all","slow"})
public class RakeBuildScriptMessagesTest extends AbstractRakeRunnerTest {

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    setMockingOptions(MockingOptions.FAKE_STACK_TRACE, MockingOptions.FAKE_LOCATION_URL);
  }

  public void testBuildScript_stdout() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_out", true, "app1");
  }

  public void testBuildScript_stdout2() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_out2", true, "app1");
  }

  public void testBuildScript_stdout3() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_out3", true, "app1");
  }

  public void testBuildScript_stdout_external() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_out_external", true, "app1");
  }

  public void testBuildScript_stderr() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_err", true, "app1");
  }

  public void testBuildScript_stderr2() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_err2", true, "app1");
  }

  public void testBuildScript_std_out_err_wo_newline() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_out_err_wo_newline", true, "app1");
  }

  public void testBuildScript_stderr_external() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:std_err_external", true, "app1");
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

  public void testBuildScript_unexistent_task() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_LOCATION_URL, FAKE_STACK_TRACE, FAKE_ERROR_MSG);

    initAndDoTest("build_script:unexistent_task", false, "app1");
  }

  public void testBuildScript_unexistent_task_real() throws Throwable {
    setPartialMessagesChecker();

    initAndDoRealTest("build_script:unexistent_task", false, "app1");
  }

  public void testBuildScript_unexistent_task_stacktrace() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_LOCATION_URL, FAKE_STACK_TRACE, FAKE_ERROR_MSG);

    initAndDoTest("build_script:unexistent_task", "_stacktrace", false, "app1");
  }

  public void testBuildScript_exception_in_task() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_LOCATION_URL, FAKE_STACK_TRACE, FAKE_ERROR_MSG);

    initAndDoTest("build_script:exception_in_task", false, "app1");
  }

  public void testBuildScript_exception_in_task_real() throws Throwable {
    setPartialMessagesChecker();

    initAndDoRealTest("build_script:exception_in_task", false, "app1");
  }

  public void testBuildScript_exception_in_embedded_task() throws Throwable {
    setPartialMessagesChecker();
    setMockingOptions(FAKE_LOCATION_URL, FAKE_STACK_TRACE, FAKE_ERROR_MSG);

    //TODO: by some reason containing block is missed...
    initAndDoTest("build_script:exception_in_embedded_task", false, "app1");
  }

  public void testBuildScript_exception_in_embedded_task_real() throws Throwable {
    setPartialMessagesChecker();

    initAndDoRealTest("build_script:exception_in_embedded_task", false, "app1");
  }

  public void testBuildScript_exception_in_embedded_task_trace() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();
    setMockingOptions(FAKE_LOCATION_URL, FAKE_STACK_TRACE, FAKE_ERROR_MSG);

    initAndDoTest("build_script:exception_in_embedded_task", "_trace", false, "app1");
  }

  public void testBuildScript_warning_in_task_in_task() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:warning_in_task", true, "app1");
  }

  public void testBuildScript_default_task() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("", "build_script/default_task", true, "app1");
  }

  public void testBuildScript_compile_error_task() throws Throwable {
    setPartialMessagesChecker();

    setMockingOptions(FAKE_STACK_TRACE, FAKE_LOCATION_URL);
    initAndDoTest("compile_error:some_task", false, "app2");
  }

  public void testBuildScript_first_time_check() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();

    initAndDoTest("build_script:first_time_check", true, "app1");
  }

  public void testBuildScript_embedded_tasks() throws Throwable {
    setPartialMessagesChecker();
    initAndDoTest("build_script:embedded_tasks", true, "app1");
  }

  public void testBuildScript_embedded_tasks_trace() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();

    initAndDoTest("build_script:embedded_tasks", "_trace", true, "app1");
  }

  public void testBuildScript_cmd_failed() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();
    setMockingOptions(FAKE_LOCATION_URL, FAKE_STACK_TRACE, FAKE_ERROR_MSG);

    initAndDoTest("build_script:cmd_failed", false, "app1");
  }

  public void testBuildScript_cmd_failed_real() throws Throwable {
    setPartialMessagesChecker();
    rakeUI_EnableTraceOption();

    initAndDoRealTest("build_script:cmd_failed", false, "app1");
  }

  public void testBuildScript_task_args() throws Throwable {
    setPartialMessagesChecker();

    initAndDoTest("build_script:task_args", true, "app1");
  }
}
