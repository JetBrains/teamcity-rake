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

package jetbrains.slow.plugins.rakerunner;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Vladislav.Rassokhin
 */
public class RubyVersionsDataProvider {
  @DataProvider(name = "ruby-versions")
  public static Iterator<Object[]> getRubyVersionsDP() {
    return getCartesianProductIterator(getRubyVersionsSet());
  }

  @DataProvider(name = "ruby-versions-linux")
  public static Iterator<Object[]> getRubyVersionsLinuxDP() {
    return getCartesianProductIterator(getRubyVersionsLinuxSet());
  }

  @DataProvider(name = "ruby-versions-windows")
  public static Iterator<Object[]> getRubyVersionsWindowsDP() {
    return getCartesianProductIterator(getRubyVersionsWindowsSet());
  }

  @NotNull
  public static Set<String> getRubyVersionsSet() {
    if (SystemInfo.isWindows) {
      return getRubyVersionsWindowsSet();
    }
    if (SystemInfo.isUnix) {
      return getRubyVersionsLinuxSet();
    }
    throw new IllegalStateException("Unsupported OS type " + System.getProperty("os.name").toLowerCase());
  }

  @NotNull
  public static Set<String> getRubyVersionsLinuxSet() {
    return new HashSet<String>() {
      {
        add("ruby-1.8.7");
        add("ruby-1.9.2");
        add("jruby");
      }
    };
  }

  @NotNull
  public static Set<String> getRubyVersionsWindowsSet() {
    return new HashSet<String>() {
      {
        add("ruby-1.8.7");
        add("ruby-1.9.2");
        add("jruby-1.6.4");
      }
    };
  }

  @NotNull
  public static Iterator<Object[]> getCartesianProductIterator(@NotNull final Set<String>... sources) {
    final Set<List<String>> cartesian = Sets.cartesianProduct(sources);
    final List<Object[]> list = CollectionsUtil.convertCollection(cartesian, new Converter<Object[], List<String>>() {
      public Object[] createFrom(@NotNull final List<String> source) {
        return source.toArray(new Object[source.size()]);
      }
    });
    return list.iterator();
  }

}
