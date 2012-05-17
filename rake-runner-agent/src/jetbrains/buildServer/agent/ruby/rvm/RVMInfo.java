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

package jetbrains.buildServer.agent.ruby.rvm;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * "rvm info" for one interpreter
 *
 * @author Vladislav.Rassokhin
 */
public class RVMInfo {

  private String myInterpreterName;
  private Map<Section, Map<String, String>> mySectionsValues = new EnumMap<Section, Map<String, String>>(Section.class);

  public RVMInfo(@NotNull final String interpreterNameWithGemset) {
    final int i = interpreterNameWithGemset.indexOf("@");
    if (i != -1) {
      myInterpreterName = new String(interpreterNameWithGemset.substring(0, i));
    } else {
      myInterpreterName = interpreterNameWithGemset;
    }
  }

  public enum Section {
    system,
    rvm,
    ruby,
    homes,
    binaries,
    environment
  }

  @NotNull
  public String getInterpreterName() {
    return myInterpreterName;
  }

  @NotNull
  public Map<String, String> getSection(@NotNull final Section section) {
    return mySectionsValues.get(section);
  }

  public void setSection(@NotNull final Section section, @NotNull final Map<String, String> values) {
    mySectionsValues.put(section, values);
  }
}
