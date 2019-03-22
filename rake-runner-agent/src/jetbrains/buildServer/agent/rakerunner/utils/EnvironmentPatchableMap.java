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

package jetbrains.buildServer.agent.rakerunner.utils;

import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class EnvironmentPatchableMap implements Map<String, String> {

  public EnvironmentPatchableMap(final Map<String, String> baseEnv) {
    myBaseEnvironment = Collections.unmodifiableMap(new HashMap<String, String>(baseEnv));
    myPatchedEnvironment = new HashMap<String, String>();
    myPatchedEnvironment.putAll(baseEnv);
    //myOperationsList = new ArrayList<Operation>();
  }

  /**
   * @return read-only map
   */
  public Map<String, String> getBase() {
    return myBaseEnvironment;
  }

  /**
   * @return modifiable map
   */
  public Map<String, String> getPatched() {
    return this;
  }

  /**
   * @return set of removed keys (exist in base and not exist in pathced)
   */
  @NotNull
  public Set<String> getRemovedKeys() {
    final Set<String> removed = new HashSet<String>(Math.max(0, getBase().size() - size()));
    for (String key : getBase().keySet()) {
      if (!containsKey(key)) {
        removed.add(key);
      }
    }
    return removed;
  }

  //private class Operation {
  //  OperationType type;
  //  String key;
  //  String value; // Null if type == Unset
  //}
  //
  //private enum OperationType {
  //  Set,
  //  Unset;
  //}

  // TODO: add calculate patch method

  private final Map<String, String> myBaseEnvironment;
  private final Map<String, String> myPatchedEnvironment;

  //private final List<Operation> myOperationsList;


  public int size() {
    return myPatchedEnvironment.size();
  }

  public boolean isEmpty() {
    return myPatchedEnvironment.isEmpty();
  }

  public boolean containsKey(final Object key) {
    return myPatchedEnvironment.containsKey(key);
  }

  public boolean containsValue(final Object value) {
    return myPatchedEnvironment.containsValue(value);
  }

  public String get(final Object key) {
    return myPatchedEnvironment.get(key);
  }

  public String put(final String key, final String value) {
    return myPatchedEnvironment.put(key, value);
  }

  public String remove(final Object key) {
    return myPatchedEnvironment.remove(key);
  }

  public void putAll(final Map<? extends String, ? extends String> m) {
    myPatchedEnvironment.putAll(m);
  }

  public void clear() {
    myPatchedEnvironment.clear();
  }

  public Set<String> keySet() {
    return myPatchedEnvironment.keySet();
  }

  public Collection<String> values() {
    return myPatchedEnvironment.values();
  }

  public Set<Entry<String, String>> entrySet() {
    return myPatchedEnvironment.entrySet();
  }
}
