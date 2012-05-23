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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RubyEnvConfiguratorConfiguration {

  public static enum Type {
    OFF,
    INTERPRETER_PATH,
    RVM,
    RVMRC
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
    this.type = RubyEnvConfiguratorUtil.getFeatureWorkingType(configParameters);
    this.shouldFailBuildIfNoSdkFound = RubyEnvConfiguratorUtil.shouldFailBuildIfNoSdkFound(configParameters);
    this.myRubySdkPath = RubyEnvConfiguratorUtil.getRubySdkPath(configParameters);
    this.myRVMSdkName = RubyEnvConfiguratorUtil.getRVMSdkName(configParameters);
    this.myRVMGemsetName = RubyEnvConfiguratorUtil.getRVMGemsetName(configParameters);
    this.myRVMRCFilePath = RubyEnvConfiguratorUtil.getRVMRCFilePath(configParameters);
    this.myRVMGemsetCreate = RubyEnvConfiguratorUtil.isRVMGemsetCreateIfNonExists(configParameters);
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
