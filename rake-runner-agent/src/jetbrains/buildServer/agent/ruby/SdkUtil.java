/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby;

import jetbrains.buildServer.agent.ruby.rbenv.RbEnvRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubySdk;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class SdkUtil {

  public static boolean isRvmSdk(@NotNull final RubySdk sdk) {
    return sdk instanceof RVMRubySdk;
  }
  public static boolean isRbEnvSdk(@NotNull final RubySdk sdk) {
    return sdk instanceof RbEnvRubySdk;
  }
}
