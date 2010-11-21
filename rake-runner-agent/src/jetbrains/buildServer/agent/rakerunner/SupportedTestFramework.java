/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner;

import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.agent.rakerunner.utils.ConfigurationParamsUtil;

/**
* @author Roman.Chernyatchik
*/

public enum SupportedTestFramework {
  TEST_UNIT(":test_unit", RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY),
  TEST_SPEC(":test_spec", RakeRunnerConstants.SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY),
  SHOULDA(":shoulda", RakeRunnerConstants.SERVER_UI_RAKE_SHOULDA_ENABLED_PROPERTY),
  RSPEC(":rspec", RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY),
  CUCUMBER(":cucumber", RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY);

  private final String myFrameworkId;
  private final String myFrameworkUIProperty;

  SupportedTestFramework(final String frameworkId,
                         final String frameworkUIProperty) {
    myFrameworkId = frameworkId;
    myFrameworkUIProperty = frameworkUIProperty;
  }

  public String getFrameworkId() {
    return myFrameworkId;
  }

  public String getFrameworkUIProperty() {
    return myFrameworkUIProperty;
  }

  public static void convertOptionsIfNecessary(final Map<String, String> runParams) {
    final String versionString = runParams.get(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY);

    int version;
    try {
      version = versionString != null ? Integer.parseInt(versionString) : 0;
    } catch (NumberFormatException ex) {
      version = 0;
    }

    if (version < 2) {
      // support for old version of rake-runner plugin
      // let's think that Test::Unit and RSpec frameworks are need to be activated
      TEST_UNIT.activate(runParams);
      RSPEC.activate(runParams);
    }
  }

  public boolean isActivated(final Map<String, String> runParams) {
    return ConfigurationParamsUtil.isParameterEnabled(runParams, getFrameworkUIProperty());
  }
  public void activate(final Map<String, String> runParams) {
    ConfigurationParamsUtil.setParameterEnabled(runParams, getFrameworkUIProperty(), true);
  }

  public static String getActivatedFrameworksConfig(final Map<String, String> runParams) {
    final StringBuilder buff = new StringBuilder();

    for (SupportedTestFramework framework : SupportedTestFramework.values()) {
      if (framework.isActivated(runParams)) {
        buff.append(framework.getFrameworkId()).append(' ');
      }
    }
    return buff.toString();
  }

  public static boolean isAnyFrameworkActivated(final Map<String, String> runParams) {
    for (SupportedTestFramework framework : SupportedTestFramework.values()) {
      if (framework.isActivated(runParams)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isTestUnitBasedFrameworksActivated(final Map<String, String> runParams) {
    return TEST_UNIT.isActivated(runParams)
           || TEST_SPEC.isActivated(runParams)
           || SHOULDA.isActivated(runParams);
  }
}