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

package jetbrains.buildServer.agent.rakerunner;

import java.util.Map;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class SharedParams implements SharedRubyEnvSettings {
  @NotNull private SharedParamsType myType = SharedParamsType.NOT_SETTED;
  @Nullable private String myInterpreterPath;
  @Nullable private String myRVMSdkName;
  @Nullable private String myRVMGemsetName;
  @Nullable private String myRVMRCPath;
  @Nullable private String myRbEnvVersion;
  @Nullable private String myRbEnvVersionFile;
  private boolean myRVMGemsetCreate;
  private boolean isApplied = false;
  @Nullable private String myRVMRubyVersionPath;


  @NotNull
  public RubySdk createSdk(@NotNull final BuildRunnerContext context) throws RakeTasksBuildService.MyBuildFailureException {
    return getType().createSdk(context, this);
  }

  public void applyToParameters(@NotNull final Map<String, String> params) {
    params.put(SHARED_RUBY_PARAMS_TYPE, getType().getValue());
    params.put(SHARED_RUBY_PARAMS_ARE_APPLIED, Boolean.toString(isApplied()));
    switch (myType) {
      case INTERPRETER_PATH: {
        params.put(SHARED_RUBY_INTERPRETER_PATH, getInterpreterPath());
        break;
      }
      case RVM: {
        params.put(SHARED_RUBY_RVM_SDK_NAME, getRVMSdkName());
        params.put(SHARED_RUBY_RVM_GEMSET_NAME, getRVMGemsetName());
        break;
      }
      case RVMRC: {
        params.put(SHARED_RUBY_RVM_RVMRC_PATH, getRVMRCPath());
        break;
      }
      case RVM_RUBY_VERSION: {
        params.put(SHARED_RUBY_RVM_RUBY_VERSION_PATH, getRVMRubyVersionPath());
      }
      case RBENV: {
        params.put(SHARED_RUBY_RBENV_VERSION_NAME, getRbEnvVersion());
        break;
      }
      case RBENV_FILE: {
        params.put(SHARED_RUBY_RBENV_FILE_PATH, getRbEnvVersionFile());
        break;
      }
      case DEFAULT: {
        // Nothing special
      }
      case NOT_SETTED: {
        // Nothing special
      }
    }
  }

  public void applyToContext(@NotNull final BuildRunnerContext context) {
    context.addRunnerParameter(SHARED_RUBY_PARAMS_TYPE, getType().getValue());
    context.addRunnerParameter(SHARED_RUBY_PARAMS_ARE_APPLIED, Boolean.toString(isApplied()));
    switch (myType) {
      case INTERPRETER_PATH: {
        context.addRunnerParameter(SHARED_RUBY_INTERPRETER_PATH, StringUtil.emptyIfNull(getInterpreterPath()));
        break;
      }
      case RVM: {
        context.addRunnerParameter(SHARED_RUBY_RVM_SDK_NAME, StringUtil.emptyIfNull(getRVMSdkName()));
        context.addRunnerParameter(SHARED_RUBY_RVM_GEMSET_NAME, StringUtil.emptyIfNull(getRVMGemsetName()));
        context.addRunnerParameter(SHARED_RUBY_RVM_GEMSET_CREATE, String.valueOf(isRVMGemsetCreate()));
        break;
      }
      case RVMRC: {
        context.addRunnerParameter(SHARED_RUBY_RVM_RVMRC_PATH, StringUtil.emptyIfNull(getRVMRCPath()));
        break;
      }
      case RVM_RUBY_VERSION: {
        context.addRunnerParameter(SHARED_RUBY_RVM_RUBY_VERSION_PATH, StringUtil.emptyIfNull(getRVMRubyVersionPath()));
      }
      case RBENV: {
        context.addRunnerParameter(SHARED_RUBY_RBENV_VERSION_NAME, StringUtil.emptyIfNull(getRbEnvVersion()));
        break;
      }
      case RBENV_FILE: {
        context.addRunnerParameter(SHARED_RUBY_RBENV_FILE_PATH, StringUtil.emptyIfNull(getRbEnvVersionFile()));
        break;
      }
      case DEFAULT: {
        // Nothing special
      }
      case NOT_SETTED: {
        // Nothing special
      }
    }
  }


  public boolean isApplied() {
    return isApplied;
  }

  public void setApplied(final boolean applied) {
    isApplied = applied;
  }

  public boolean isSetted() {
    return myType != SharedParamsType.NOT_SETTED;
  }

  @Nullable
  public String getRVMRCPath() {
    return myRVMRCPath;
  }

  public void setRVMRCPath(@Nullable final String RVMRCPath) {
    myRVMRCPath = RVMRCPath;
  }

  @Nullable
  public String getRVMGemsetName() {
    return myRVMGemsetName;
  }

  public void setRVMGemsetName(@Nullable final String RVMGemsetName) {
    myRVMGemsetName = RVMGemsetName;
  }

  @Nullable
  public String getRVMSdkName() {
    return myRVMSdkName;
  }

  public void setRVMSdkName(@Nullable final String RVMSdkName) {
    myRVMSdkName = RVMSdkName;
  }

  @Nullable
  public String getInterpreterPath() {
    return myInterpreterPath;
  }

  public void setInterpreterPath(@Nullable final String interpreterPath) {
    myInterpreterPath = interpreterPath;
  }

  @NotNull
  public SharedParamsType getType() {
    return myType;
  }

  public void setType(@NotNull final SharedParamsType type) {
    myType = type;
  }

  public void setRVMGemsetCreate(final boolean create) {
    myRVMGemsetCreate = create;
  }

  public boolean isRVMGemsetCreate() {
    return myRVMGemsetCreate;
  }

  @NotNull
  public static SharedParams fromRunParameters(@NotNull final Map<String, String> runParams) {
    SharedParams shared = new SharedParams();
    shared.setApplied(Boolean.valueOf(runParams.get(SHARED_RUBY_PARAMS_ARE_APPLIED)));
    shared.setType(getParamsType(runParams));
    shared.setInterpreterPath(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_INTERPRETER_PATH)));
    shared.setRVMSdkName(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_RVM_SDK_NAME)));
    shared.setRVMGemsetName(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_RVM_GEMSET_NAME)));
    shared.setRVMRCPath(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_RVM_RVMRC_PATH)));
    shared.setRVMGemsetCreate(Boolean.valueOf(runParams.get(SHARED_RUBY_RVM_GEMSET_CREATE)));
    shared.setRVMRubyVersionPath(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_RVM_RUBY_VERSION_PATH)));
    shared.setRbEnvVersion(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_RBENV_VERSION_NAME)));
    shared.setRbEnvVersionFile(StringUtil.trimAndNull(runParams.get(SHARED_RUBY_RBENV_FILE_PATH)));
    return shared;
  }

  @NotNull
  private static SharedParamsType getParamsType(@NotNull final Map<String, String> runParameters) {
    final String typeString = runParameters.get(SHARED_RUBY_PARAMS_TYPE);
    if (StringUtil.isEmptyOrSpaces(typeString)) return SharedParamsType.NOT_SETTED;

    for (SharedParamsType type : SharedParamsType.values()) {
      if (type.getValue().equalsIgnoreCase(typeString)) {
        return type;
      }
    }
    return SharedParamsType.NOT_SETTED;
  }

  @Nullable
  public String getRbEnvVersion() {
    return myRbEnvVersion;
  }

  public void setRbEnvVersion(@Nullable final String rbEnvVersion) {
    myRbEnvVersion = rbEnvVersion;
  }

  @Nullable
  public String getRbEnvVersionFile() {
    return myRbEnvVersionFile;
  }

  public void setRbEnvVersionFile(@Nullable final String rbEnvVersionFile) {
    myRbEnvVersionFile = rbEnvVersionFile;
  }

  @Nullable
  public String getRVMRubyVersionPath() {
    return myRVMRubyVersionPath;
  }

  public void setRVMRubyVersionPath(@Nullable final String path) {
    myRVMRubyVersionPath = path;
  }
}
