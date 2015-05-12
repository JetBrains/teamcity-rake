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

package jetbrains.buildServer.runner.rakerunner;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Vladislav.Rassokhin
 */
@Test(groups = "all")
public class RakeRunnerRunTypeTest {

  public static final PropertiesProcessor PARAMETERS_PROCESSOR = new RakeRunnerRunType.ParametersValidator();

  @Test
  public void testRVMInterpreterNameValidation() throws Exception {
    assertTrue(isRVMInterpreterNameValid("ruby-1.9.3"));
    assertTrue(isRVMInterpreterNameValid("jruby"));
    assertTrue(isRVMInterpreterNameValid("%var%"));
    assertFalse(isRVMInterpreterNameValid(""));
    assertFalse(isRVMInterpreterNameValid(null));
  }

  private boolean isRVMInterpreterNameValid(@Nullable final String name) {
    return PARAMETERS_PROCESSOR.process(createRVMConfiguration(name)).isEmpty();
  }

  private Map<String, String> createRVMConfiguration(@Nullable final String name) {
    Map<String, String> map = new HashMap<String, String>();
    map.put(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE, RakeRunnerUtils.RubyConfigMode.RVM.getModeValueString());
    map.put(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, name);
    return map;
  }
}
