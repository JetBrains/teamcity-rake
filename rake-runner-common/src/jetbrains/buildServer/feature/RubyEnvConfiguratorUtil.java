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

package jetbrains.buildServer.feature;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Roman.Chernyatchik
 */
public class RubyEnvConfiguratorUtil {
  public final static String RUBY_ENV_CONFIGURATOR_KEY = "teamcity.ruby.configurator.enabled";
  public static final String UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY = "ui.ruby.configurator.fail.build.if.interpreter.not.found";

  public static final String UI_RVM_GEMSET_NAME_KEY = "ui.ruby.configurator.rvm.gemset.name";
  public static final String UI_RVM_SDK_NAME_KEY = "ui.ruby.configurator.rvm.sdk.name";
  public static final String UI_RVM_RVMRC_PATH_KEY = "ui.ruby.configurator.rvm.rvmrc.path";
  public static final String UI_USE_RVM_KEY = "ui.ruby.configurator.use.rvm";
  public static final String UI_RUBY_SDK_PATH_KEY = "ui.ruby.configurator.ruby.interpreter.path";

  public static boolean shouldFailBuildIfNoSdkFound(@NotNull final Map<String, String> params) {
    final String key = UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY;
    return Boolean.parseBoolean(params.get(key));
  }

  @Nullable
  public static String getRubySdkPath(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RUBY_SDK_PATH_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMGemsetName(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RVM_GEMSET_NAME_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMSdkName(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RVM_SDK_NAME_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMRCFilePath(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RVM_RVMRC_PATH_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @NotNull
  public static RubyEnvConfiguratorConfiguration.Type getFeatureWorkingType(@NotNull final Map<String, String> params) {
    if (StringUtil.isEmpty(params.get(RUBY_ENV_CONFIGURATOR_KEY))) return RubyEnvConfiguratorConfiguration.Type.OFF;

    final String useRVMType = params.get(UI_USE_RVM_KEY);
    if ("manual".equals(useRVMType)) {
      return RubyEnvConfiguratorConfiguration.Type.RVM;
    } else if ("rvmrc".equals(useRVMType)) {
      return RubyEnvConfiguratorConfiguration.Type.RVMRC;
    } else {
      return RubyEnvConfiguratorConfiguration.Type.INTERPRETER_PATH;
    }
  }

}
