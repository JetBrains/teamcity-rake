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
    RVMRC, // "rvmrc"
    RVM_RUBY_VERSION, // "rvm_ruby_version"
    RBENV, // "rbenv"
    RBENV_FILE, // ".rbenv-version"
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

  @Nullable private final String myRVMRubyVersionPath;

  @Nullable private final String myRbEnvVersion;
  @Nullable private final String myRbEnvVersionFile;

  public RubyEnvConfiguratorConfiguration(final Map<String, String> configParameters) {
    final String type = configParameters.get(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY);
    if ("unspecified".equals(type)) {
      throw new IllegalStateException(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY + " must be specified");
    }
    if ("manual".equals(type)) {
      this.type = Type.RVM;
    } else if ("rvmrc".equals(type)) {
      this.type = Type.RVMRC;
    } else if ("rvm_ruby_version".equals(type)) {
      this.type = Type.RVM_RUBY_VERSION;
    } else if ("rbenv".equals(type)) {
      this.type = Type.RBENV;
    } else if ("rbenv_file".equals(type)) {
      this.type = Type.RBENV_FILE;
    } else {
      this.type = Type.INTERPRETER_PATH;
    }
    this.shouldFailBuildIfNoSdkFound =
      Boolean.parseBoolean(configParameters.get(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY));
    this.myRubySdkPath = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY));
    this.myRVMSdkName = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY));
    this.myRVMGemsetName = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY));
    this.myRVMRCFilePath = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY));
    this.myRVMGemsetCreate = Boolean.parseBoolean(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS));
    this.myRbEnvVersion = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RBENV_VERSION_NAME_KEY));
    this.myRbEnvVersionFile = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RBENV_FILE_PATH_KEY));
    this.myRVMRubyVersionPath = StringUtil.nullIfEmpty(configParameters.get(RubyEnvConfiguratorConstants.UI_RVM_RUBY_VERSION_PATH_KEY));
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

  @Nullable
  public String getRVMRubyVersionPath() {
    return myRVMRubyVersionPath;
  }

  @Nullable
  public String getRbEnvVersion() {
    return myRbEnvVersion;
  }

  @Nullable
  public String getRbEnvVersionFile() {
    return myRbEnvVersionFile;
  }
}
