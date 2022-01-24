/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Anna Bulenkova
 * @author Vladislav.Rassokhin
 */
public abstract class RubyVersionManager {
  private final String myName;

  protected RubyVersionManager(@NotNull final String name) {
    myName = name;
  }

  @Nullable
  public abstract File getHome();

  @Nullable
  public abstract File getRubiesFolder();

  public abstract boolean isSupportedByOs();

  @NotNull
  public String getName() {
    return myName;
  }
}