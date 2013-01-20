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

package jetbrains.buildServer.agent.rakerunner.utils;

import java.util.*;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class EnvUtil {
  private static final Set<String> IGNORING_ENV_KEYS = CollectionsUtil.setOf("PWD", "OLDPWD");

  @NotNull
  public static Map<String, String> parse(@NotNull final String stdout) {
    final HashMap<String, String> map = new HashMap<String, String>();
    for (String line : stdout.split("\n")) {
      final int i = line.indexOf('=');
      if (i <= 0) continue;
      String name = line.substring(0, i);
      String value = line.substring(i + 1, line.length());
      map.put(name, value);
    }
    return map;
  }

  @NotNull
  public static Map<String, String> mergeIntoNewEnv(@NotNull final Map<String, String> modified,
                                                    @NotNull final Map<String, String> original,
                                                    @NotNull final Collection<String> restricted) {
    final HashMap<String, String> map = new HashMap<String, String>();
    for (String key : modified.keySet()) {
      final String ov = original.get(key);
      if (restricted.contains(key)) {
        continue;
      }
      final String mv = modified.get(key);
      if (ov == null || !ov.equals(mv)) {
        map.put(key, mv);
      } else {
        map.put(key, ov);
      }
    }
    for (String key : restricted) {
      final String ov = original.get(key);
      if (ov != null) {
        map.put(key, ov);
      }
    }
    return map;
  }

  @NotNull
  public static Map<String, String> getCompactEnvMap(@NotNull final Map<String, String> a) {
    final Map<String, String> na = new TreeMap<String, String>(a);
    na.keySet().removeAll(IGNORING_ENV_KEYS);
    return na;
  }
}
