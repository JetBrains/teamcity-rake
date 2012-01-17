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

package jetbrains.buildServer.agent.ruby.rvm.detector;

import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Base class for "RVM Detector" - an utility designed for
 * detecting RVM installation on agent.
 *
 * @author Vladislav.Rassokhin
 */
public abstract class RVMDetector {

  public static final String CONF_PARAMETER_PREFIX = "rvm.";
  public static final String CONF_RVM_RUBIES_LIST = CONF_PARAMETER_PREFIX + "rubies.list";
  public static final String CONF_RVM_VERSION = CONF_PARAMETER_PREFIX + "installation.version";
  public static final String CONF_RVM_PATH = CONF_PARAMETER_PREFIX + "installation.path";
  public static final String CONF_RVM_TYPE = CONF_PARAMETER_PREFIX + "installation.type";
  public static final String CONF_RVM_EXIST = CONF_PARAMETER_PREFIX + "exist";

  protected void init() {
  }

  /**
   * That function detects installed RVM.
   *
   * @param environmentParams environment variables map
   * @return founded RVM installation or null if RVM does not found
   */
  @Nullable
  public abstract InstalledRVM detect(@NotNull final Map<String, String> environmentParams);

  @NotNull
  public Map<String, String> createConfigurationParameters(@Nullable final InstalledRVM rvm) {
    if (rvm == null) {
      return Collections.emptyMap();
    }
    final SortedMap<String, String> params = new TreeMap<String, String>();

    params.put("env.rvm_path", rvm.getPath());
//    params.put(CONF_RVM_EXIST, "true");
//    params.put(CONF_RVM_TYPE, rvm.getType().name().toLowerCase());
//    params.put(CONF_RVM_PATH, rvm.getPath());
//    params.put(CONF_RVM_VERSION, rvm.getVersion());
//
//    StringBuilder allVersions = new StringBuilder();
//    for (String rubyName : rvm.getRubiesNames()) {
//      allVersions.append(rubyName).append(',');
//    }
//
//    params.put(CONF_RVM_RUBIES_LIST, StringUtil.trimEnd(allVersions.toString(), ","));
    return params;
  }
}
