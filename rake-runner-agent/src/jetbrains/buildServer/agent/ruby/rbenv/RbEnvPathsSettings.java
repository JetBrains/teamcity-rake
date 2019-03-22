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

package jetbrains.buildServer.agent.ruby.rbenv;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.rbenv.detector.RbEnvDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RbEnvPathsSettings {
  private static RbEnvPathsSettings ourInstance;

  @NotNull
  private final RbEnvDetector myDetector;

  @Nullable
  private InstalledRbEnv myInstalledRbEnv;

  public RbEnvPathsSettings(@NotNull final RbEnvDetector detector) {
    myDetector = detector;
    ourInstance = this;
  }

  public static RbEnvPathsSettings getInstance() {
    return ourInstance;
  }


  public void initialize(@NotNull final Map<String, String> env) {
    myInstalledRbEnv = myDetector.detect(env);
  }

  @Nullable
  public InstalledRbEnv getRbEnv() {
    return myInstalledRbEnv;
  }

  /**
   * Null-safe version of (getInstance().getRbEnv()) for getting current known rbenv.
   * Be sure that rbenv known at execution time.
   *
   * @return known rvm
   * @throws IllegalStateException if rvm is null
   */
  @NotNull
  public static InstalledRbEnv getRbEnvNullSafe() {
    final InstalledRbEnv rvm = getInstance().getRbEnv();
    if (rvm == null) {
      throw new IllegalStateException("Unexpected: rbenv is null. Cannot be null at that step.");
    }
    return rvm;
  }
}
