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

package jetbrains.buildServer.runner.rakerunner;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman Chernyatchik
 */
public class RakeRunnerRunType extends RunType {

  public RakeRunnerRunType(final RunTypeRegistry runTypeRegistry) {
    runTypeRegistry.registerRunType(this);
  }

  @Override
  @Nullable
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    // Do nothing
    return null;
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return "taskRunnerRunParams.jsp";
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return "viewTaskRunnerRunParams.jsp";
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    final Map<String, String> map = new HashMap<String, String>();

    final String trueStr = Boolean.TRUE.toString();

    // by default let's enable : Test::Unit, RSpec, Cucumber
    map.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY, trueStr);
    map.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY, trueStr);
    map.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY, trueStr);

    // configuration version
    map.put(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY,
            RakeRunnerConstants.CURRENT_CONFIG_VERSION);

    // select ruby interpreter path mode by default:
    RakeRunnerUtils.setConfigMode(RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH, map);
    return map;
  }

  @Override
  public String getDescription() {
    return RakeRunnerBundle.RUNNER_DESCRIPTION;
  }

  @Override
  public String getDisplayName() {
    return RakeRunnerBundle.RUNNER_DISPLAY_NAME;
  }

  @NotNull
  @Override
  public String getType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull final Map<String, String> parameters) {
    StringBuilder result = new StringBuilder();
    if (parameters.get("use-custom-build-file") != null) {
      result.append("Rake file: custom");
    } else {
      result.append("Rake file path: ").append(StringUtil.emptyIfNull(parameters.get("build-file-path")));
    }
    result.append("\n");

    final String tasks = parameters.get(RakeRunnerConstants.SERVER_UI_RAKE_TASKS_PROPERTY);
    result.append("Rake tasks: ").append(StringUtil.isEmpty(tasks) ? "default" : tasks);
    result.append("\n");

    switch (RakeRunnerUtils.getRubyInterpreterConfigMode(parameters)) {
      case DEFAULT:
        result.append("Default Ruby interpreter");
        break;
      case INTERPRETER_PATH:
        final String rubyInterpreterPath = RakeRunnerUtils.getRubySdkPath(parameters);
        result.append("Ruby interpreter: ").append(rubyInterpreterPath);
        break;
      case RVM:
        final String rvmSdkName = RakeRunnerUtils.getRVMSdkName(parameters);

        result.append("RVM interpreter: ").append(rvmSdkName != null ? rvmSdkName
                                                                     : RakeRunnerBundle.DEFAULT_RVM_SDK);
        final String gemset = RakeRunnerUtils.getRVMGemsetName(parameters);
        if (gemset != null) {
          result.append('@').append(gemset);
        }
        break;
    }
    return result.toString();
  }
}