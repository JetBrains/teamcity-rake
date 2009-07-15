/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.serverSide.SBuildType;

/**
 * @author Roman Chernyatchik
 */
public class RakeRunnerRunType extends RunType {

  public RakeRunnerRunType(final RunTypeRegistry runTypeRegistry) {
    runTypeRegistry.registerRunType(this);
  }

  public PropertiesProcessor getRunnerPropertiesProcessor() {
    // Do nothing
    return null;
  }

  public String getEditRunnerParamsJspFilePath() {
    return "taskRunnerRunParams.jsp";
  }

  public String getViewRunnerParamsJspFilePath() {
    return "viewTaskRunnerRunParams.jsp";
  }

  public Map<String, String> getDefaultRunnerProperties() {
    final Map<String, String> map = new HashMap<String, String>();

    final String trueStr = Boolean.TRUE.toString();

    // by default let's enable : Test::Unit, RSpec, Cucumber
    map.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY, trueStr);
    map.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY, trueStr);
    map.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY, trueStr);

    return map;
  }

  public boolean isCheckoutTypeSupported(final SBuildType.CheckoutType checkoutType) {
    return true;
  }

  public String getDescription() {
    return RakeRunnerBundle.RUNNER_DESCRIPTION;
  }

  public String getDisplayName() {
    return RakeRunnerBundle.RUNNER_DISPLAY_NAME;
  }

  public String getType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }
}