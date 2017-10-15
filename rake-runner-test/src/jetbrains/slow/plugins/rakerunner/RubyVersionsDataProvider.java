/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
import java.io.IOException;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.agent.rakerunner.utils.FileUtil2;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import jetbrains.buildServer.agent.ruby.rvm.detector.RVMDetector;
import jetbrains.buildServer.util.*;
import jetbrains.buildServer.util.impl.Lazy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.*;

/**
 * @author Vladislav.Rassokhin
 */
public class RubyVersionsDataProvider {
  private static final String[] RUBY_VERSION_PRIORITY = new String[]{"ruby-2.4", "ruby-2.3", "ruby-2.2","ruby-2.1", "ruby-2.0", "ruby-1.9", "ruby-1.8", "jruby"};
  private static final Lazy<String> ourExistentRVMRubyVersion = new Lazy<String>() {
    @Nullable
    @Override
    protected String createValue() {
      String property = System.getProperty("ruby.testing.versions");
      if (property != null) {
        final List<String> rubies = StringUtil.split(property, " ");
        final String proposed = getInPriority(rubies, RUBY_VERSION_PRIORITY);
        if (proposed != null) {
          return proposed;
        }
      }
      final Properties config = getRunningBuildConfigProperties();
      if (config != null) {
        property = config.getProperty(RVMDetector.CONF_RVM_RUBIES_LIST);
        if (property != null) {
          final String proposed = getInPriority(StringUtil.split(property, ","), RUBY_VERSION_PRIORITY);
          if (proposed != null) {
            return proposed;
          }
        }
      }
      return "ruby-2.2.5";
    }
  };

  @DataProvider(name = "ruby-versions")
  public static Iterator<Object[]> getRubyVersionsDP() {
    //noinspection unchecked
    return getCartesianProductIterator(getRubyVersionsSet());
  }

  @DataProvider(name = "ruby-versions-linux")
  public static Iterator<Object[]> getRubyVersionsLinuxDP() {
    //noinspection unchecked
    return getCartesianProductIterator(getRubyVersionsLinuxSet());
  }

  @DataProvider(name = "ruby-versions-windows")
  public static Iterator<Object[]> getRubyVersionsWindowsDP() {
    //noinspection unchecked
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
    final String property = System.getProperty("ruby.testing.versions", null);
    if (property != null) {
      final List<String> rubies = StringUtil.split(property, " ");
      return new HashSet<String>(rubies);
    }
    if (StringUtil.isTrue(System.getProperty("rake.runner.tests.use.all.rvm.interpreters")) ||
        StringUtil.isTrue(System.getProperty("rake.runner.tests.use.all.interpreters"))) {
      if (RakeRunnerTestUtil.isUseRVM()) {
        final SortedSet<String> rubies = RakeRunnerTestUtil.getRvm().getInstalledRubies();
        // Use latest patch version
        final Map<String, String> m = new HashMap<String, String>();
        for (String ruby : rubies) {
          final String s = ruby.replaceAll("\\-p\\d+", "");
          if (VersionComparatorUtil.compare(m.get(s), ruby) < 0) {
            m.put(s, ruby);
          }
        }
        return new TreeSet<String>(m.values());
      } else if (RakeRunnerTestUtil.isUseRbEnv()) {
        final SortedSet<String> rubies = new TreeSet<String>(RakeRunnerTestUtil.getRbenv().getInstalledVersions());
        // Use latest patch version
        final Map<String, String> m = new HashMap<String, String>();
        for (String ruby : rubies) {
          final String s = ruby.replaceAll("\\-p\\d+", "");
          if (VersionComparatorUtil.compare(m.get(s), ruby) < 0) {
            m.put(s, ruby);
          }
        }
        return new TreeSet<String>(m.values());
      }
    }

    return new HashSet<String>() {
      {
        if (RakeRunnerTestUtil.isUseRVM()) {
          add("ruby-2.1");
          add("ruby-2.2");
          add("ruby-2.3");
          add("ruby-2.4");
          add("jruby");
        } else if (RakeRunnerTestUtil.isUseRbEnv()) {
          add("2.0.0-p648");
          add("2.1.10");
          add("2.2.5");
          add("2.3.1");
          add("jruby-1.7.25");
          add("jruby-9.1.2.0");
        }
      }
    };
  }

  @NotNull
  public static Set<String> getRubyVersionsWindowsSet() {
    final String storage = System.getProperty(RakeRunnerTestUtil.INTERPRETERS_STORAGE_PATH_PROPERTY);
    if (storage != null && FileUtil2.checkIfDirExists(storage)) {
      final File[] interpreters = new File(storage).listFiles(new FileFilter() {
        public boolean accept(@NotNull final File file) {
          return file.isDirectory() && isInterpreterDirectory(file);
        }
      });
      if (interpreters != null && interpreters.length > 0) {
        return CollectionsUtil.convertSet(Arrays.asList(interpreters), new Converter<String, File>() {
          public String createFrom(@NotNull final File source) {
            return source.getName();
          }
        });
      }
    }
    return new HashSet<String>() {
      {
        add("ruby-1.8.7");
        add("ruby-1.9.2");
        add("jruby-1.6.4");
      }
    };
  }

  @Contract("null -> false")
  private static boolean isInterpreterDirectory(@Nullable final File directory) {
    if (directory == null || !directory.exists() || !directory.isDirectory()) {
      return false;
    }
    final File bin = new File(directory, "bin");
    if (!bin.exists() || !bin.isDirectory()) {
      return false;
    }
    final HashSet<String> probablyNames = new HashSet<String>() {{
      if (SystemInfo.isWindows) {
        add(OSUtil.RUBY_EXE_WIN);
        add(OSUtil.RUBY_EXE_WIN_BAT);
        add(OSUtil.JRUBY_EXE_WIN);
        add(OSUtil.JRUBY_EXE_WIN_BAT);
      } else {
        add(OSUtil.JRUBY_EXE_UNIX);
        add(OSUtil.RUBY_EXE_UNIX);
      }
    }};
    final File[] files = FileUtil.listFiles(bin, new FilenameFilter() {
      public boolean accept(@NotNull final File dir, @NotNull final String name) {
        return probablyNames.contains(name);
      }
    });
    return files.length > 0;
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

  @NotNull
  protected static String getExistentRVMRubyVersion() {
    return ourExistentRVMRubyVersion.getValue();
  }

  private static String getInPriority(final Collection<String> input, String... prefixes) {
    final TreeSet<String> set = new TreeSet<String>(input);
    for (String prefix : prefixes) {
      String result = null;
      for (String ruby : set) {
        if (ruby.startsWith(prefix)) {
          result = ruby;
        }
      }
      if (result != null) return result;
    }
    return !set.isEmpty() ? set.iterator().next() : null;
  }

  @Nullable
  private static Properties getRunningBuildConfigProperties() {
    final String property = System.getProperty(AgentRuntimeProperties.AGENT_CONFIGURATION_PARAMS_FILE_PROP);
    if (property == null) return null;
    try {
      return PropertiesUtil.loadProperties(new File(property));
    } catch (IOException e) {
      return null;
    }
  }
}
