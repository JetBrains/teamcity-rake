/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner.utils.impl;

import jetbrains.buildServer.agent.rakerunner.RubyLightweightSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Roman.Chernyatchik
 */
public class RubyLightweightSdkImpl implements RubyLightweightSdk {
  private final String myInterpreterPath;
  private final boolean myIsRvmSdk;
  private final boolean myIsSystemRvm;
  private final String myGemsetName;

  public RubyLightweightSdkImpl(@NotNull final String interpreterPath,
                                   final boolean isRvmSdk,
                                   final boolean isSystemRvm,
                                   final String gemsetName) {

    myInterpreterPath = interpreterPath;
    myIsRvmSdk = isRvmSdk;
    myIsSystemRvm = isSystemRvm;
    myGemsetName = gemsetName;
  }


  @NotNull
  public String getInterpreterPath() {
    return myInterpreterPath;
  }

  public boolean isRvmSdk() {
    return myIsRvmSdk;
  }

  @Nullable
  public String getRvmGemsetName() {
    return myGemsetName;
  }

  @NotNull
  public String getPresentableName() {
    if (isRvmSdk()) {
      final String gemsetName = getRvmGemsetName();
      return myInterpreterPath + (gemsetName != null ? "[" + RVMSupportUtil.getGemsetSeparator() + gemsetName + "]" : "");
    } else {
      return myInterpreterPath;
    }
  }

  public boolean isSystemRvm() {
    return myIsSystemRvm;
  }
}
