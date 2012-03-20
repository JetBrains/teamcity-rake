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

package org.jetbrains.plugins.ruby.rvm;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.agent.ruby.rvm.detector.RVMDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class RVMPathsSettings extends SharedRVMPathsSettings {
  private static RVMPathsSettings ourInstance;

  @NotNull
  private final RVMDetector myDetector;

  @Nullable
  private InstalledRVM myInstalledRVM;

  public RVMPathsSettings(RVMDetector detector) {
    myDetector = detector;
    ourInstance = this;
  }

  public static SharedRVMPathsSettings getInstance() {
    return ourInstance;
  }

  public static RVMPathsSettings getInstanceEx() {
    return ourInstance;
  }

  public void initialize(@NotNull final Map<String, String> env) {
    myInstalledRVM = myDetector.detect(env);
  }

  public void initialize(@Nullable final InstalledRVM rvm) {
    myInstalledRVM = rvm;
  }

  @Override
  @Nullable
  public InstalledRVM getRVM() {
    return myInstalledRVM;
  }
}
