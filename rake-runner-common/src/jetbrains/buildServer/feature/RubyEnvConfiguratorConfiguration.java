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

import java.util.Map;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RubyEnvConfiguratorConfiguration {

  public static enum Type {
    INTERPRETER_PATH, // null
    RVM, // "manual"
    RVMRC // "rvmrc"
  }

  @NotNull
  private final Type type;

  private final boolean shouldFailBuildIfNoSdkFound;

  @Nullable
  private final String myRubySdkPath;
  @Nullable
  private final String myRVMSdkName;
  @Nullable
  private final String myRVMGemsetName;
  @Nullable
  private final String myRVMRCFilePath;

  private final boolean myRVMGemsetCreate;

  public RubyEnvConfiguratorConfiguration(final Map<String, String> configParameters) {
    final String type = configParameters.get(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY);
    if ("manual".equals(type)) {
      this.type = Type.RVM;
    } else if ("rvmrc".equals(type)) {
      this.type = Type.RVMRC;
    } else {
      this.type = Type.INTERPRETER_PATH;
    }
    this.shouldFailBuildIfNoSdkFound = Boolean.parseBoolean(configParameters.get(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY));
    this.myRubySdkPath = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY));
    this.myRVMSdkName = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY));
    this.myRVMGemsetName = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY));
    this.myRVMRCFilePath = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY));
    this.myRVMGemsetCreate = Boolean.parseBoolean(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS));
  }

  @NotNull
  public Type getType() {
    return type;
  }

  public boolean isShouldFailBuildIfNoSdkFound() {
    return shouldFailBuildIfNoSdkFound;
  }

  @Nullable
  public String getRubySdkPath() {
    return myRubySdkPath;
  }

  @Nullable
  public String getRVMSdkName() {
    return myRVMSdkName;
  }

  @Nullable
  public String getRVMGemsetName() {
    return myRVMGemsetName;
  }

  @Nullable
  public String getRVMRCFilePath() {
    return myRVMRCFilePath;
  }

  public boolean isRVMGemsetCreate() {
    return myRVMGemsetCreate;
  }
}
