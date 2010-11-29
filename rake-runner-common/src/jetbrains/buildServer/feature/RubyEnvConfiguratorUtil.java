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

package jetbrains.buildServer.feature;

import com.intellij.openapi.util.text.StringUtil;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class RubyEnvConfiguratorUtil {
  public final static String RUBY_ENV_CONFIGURATOR_KEY = "teamcity.ruby.configurator.enabled";
  public static final String UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY = "ui.ruby.configurator.fail.build.if.interpreter.not.found";

  private static final String UI_RVM_GEMSET_NAEM_KEY = "ui.ruby.configurator.rvm.gemset.name";
  private static final String UI_RVM_SDK_NAME_KEY = "ui.ruby.configurator.rvm.sdk.name";
  private static final String UI_USE_RVM_KEY = "ui.ruby.configurator.use.rvm";
  private static final String UI_RUBY_SDK_PATH_KEY = "ui.ruby.configurator.ruby.interpreter.path";

  public static boolean isRubyEnvConfiguratorEnabled(@NotNull final Map<String, String> params) {
    final String value = params.get(RUBY_ENV_CONFIGURATOR_KEY);
    return !StringUtil.isEmpty(value) && Boolean.valueOf(value);
  }

  public static boolean isRVMEnabled(@NotNull final Map<String, String> params) {
    return StringUtil.isEmpty(params.get(UI_USE_RVM_KEY));
  }

  public static boolean shouldFailBuildIfNoSdkFound(@NotNull final Map<String, String> params) {
    final String key = UI_FAIL_BUILD_IN_NO_RUBY_FOUND_KEY;

    return params.containsKey(key)
           && params.get(key).equals(Boolean.TRUE.toString());
  }

  @Nullable
  public static String getRubySdkPath(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RUBY_SDK_PATH_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMGemsetName(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RVM_GEMSET_NAEM_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMSdkName(@NotNull final Map<String, String> params) {
    final String value = params.get(UI_RVM_SDK_NAME_KEY);
    return StringUtil.isEmpty(value) ? null : value;
  }
}
