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

package jetbrains.buildServer.agent.ruby.rvm.impl;

import jetbrains.buildServer.agent.ruby.impl.RubyLightweightSdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubyLightweightSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRubyLightweightSdkImpl extends RubyLightweightSdkImpl implements RVMRubyLightweightSdk {

  @Nullable
  private final String myGemsetName;
  @NotNull
  private final String myName;

  public RVMRubyLightweightSdkImpl(@NotNull final String interpreterPath,
                                   @NotNull final String name,
                                   final boolean isSystem,
                                   @Nullable final String gemsetName) {
    super(interpreterPath, isSystem);
    myGemsetName = gemsetName;
    myName = name;
  }

  public String getRvmGemsetName() {
    return myGemsetName;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  public boolean isRvmSdk() {
    return true;
  }

  @NotNull
  @Override
  public String getPresentableName() {
    final String gemsetName = getRvmGemsetName();
    return getInterpreterPath() + (gemsetName != null ? "[" + RVMSupportUtil.getGemsetSeparator() + gemsetName + "]" : "");
  }
}
