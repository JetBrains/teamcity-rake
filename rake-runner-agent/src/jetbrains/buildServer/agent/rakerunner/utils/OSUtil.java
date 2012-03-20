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

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import static jetbrains.buildServer.util.FileUtil.toSystemDependentName;

/**
 * @author Roman.Chernyatchik
 */
public class OSUtil {
  public static String INDEPENDENT_PATH_SEPARATOR = "/";

  private static final String ENVIRONMENT_PATH_VARIABLE_NAME;

  static {
    if (SystemInfo.isWindows) {
      ENVIRONMENT_PATH_VARIABLE_NAME = "Path";
    } else if (SystemInfo.isUnix) {
      ENVIRONMENT_PATH_VARIABLE_NAME = "PATH";
    } else {
      throw new RuntimeException(RakeRunnerBundle.MSG_OS_NOT_SUPPORTED);
    }
  }

  private static final String RUBY_EXE_WIN = "ruby.exe";
  private static final String RUBY_EXE_WIN_BAT = "ruby.bat";
  private static final String RUBY_EXE_UNIX = "ruby";
  private static final String JRUBY_EXE_WIN = "jruby.exe";
  private static final String JRUBY_EXE_WIN_BAT = "jruby.bat";
  private static final String JRUBY_EXE_UNIX = "jruby";

  public static void appendToRUBYLIBEnvVariable(@NotNull final String additionalPath,
                                                @NotNull final Map<String, String> envMap) {

    // Seems RUBY interpeter expects system independent ruby separators even on Windows machines
    mergeWithEnvVariable(true,
                         additionalPath,
                         File.pathSeparator,
                         RakeRunnerConstants.RUBYLIB_ENVIRONMENT_VARIABLE, envMap,
                         false, true);
  }

  public static void prependToRUBYOPTEnvVariable(@NotNull final String additionalArgs,
                                                 @NotNull final Map<String, String> initialEnvValuesMap) {

    // Seems RUBY interpeter expects system independent ruby separators even on Windows machines
    mergeWithEnvVariable(false,
                         additionalArgs,
                         " ",
                         RakeRunnerConstants.RUBYOPT_ENVIRONMENT_VARIABLE, initialEnvValuesMap,
                         false, true);
  }

  public static void prependToPATHEnvVariable(@NotNull final String additionalPath,
                                              @NotNull final Map<String, String> initialEnvValuesMap) {

    mergeWithEnvVariable(false,
                         additionalPath,
                         File.pathSeparator,
                         getPATHEnvVariableKey(), initialEnvValuesMap,
                         true, false);
  }

  private static void mergeWithEnvVariable(final boolean append,
                                           @NotNull final String additionalValue,
                                           @NotNull final String separator,
                                           @NotNull final String variableName,
                                           @NotNull final Map<String, String> envMap,
                                           final boolean convertToSystemDependent,
                                           final boolean convertToSystemInDependent) {
    if (convertToSystemDependent && convertToSystemInDependent) {
      throw new IllegalArgumentException("Convert to system dependent and independent cant be both true.");
    }

    final String oldValue = envMap.get(variableName);

    final String mergedValue;
    if (StringUtil.isEmpty(oldValue)) {
      mergedValue = additionalValue;
    } else {
      mergedValue = append ? oldValue + separator + additionalValue
                           : additionalValue + separator + oldValue;
    }

    String newValue = mergedValue;
    if (convertToSystemDependent) {
      newValue = toSystemDependentName(mergedValue);
    }

    if (convertToSystemInDependent) {
      newValue = toSystemIndependentName(mergedValue);
    }
    envMap.put(variableName, newValue);
  }

  @Nullable
  public static String getPATHEnvVariable(@NotNull final Map<String, String> buildParameters) {
    return buildParameters.get(Constants.ENV_PREFIX + getPATHEnvVariableKey());
  }

  @NotNull
  public static String getPATHEnvVariableKey() {
    return ENVIRONMENT_PATH_VARIABLE_NAME;
  }

  @Nullable
  public static String findExecutableByNameInPATH(@NotNull final String exeName,
                                                  @NotNull final Map<String, String> buildParameters) {
    final String path = getPATHEnvVariable(buildParameters);
    if (path != null) {
      final StringTokenizer st = new StringTokenizer(path, File.pathSeparator);

      //tokens - are pathes with system-dependent slashes
      while (st.hasMoreTokens()) {
        final String possible_path = st.nextToken() + INDEPENDENT_PATH_SEPARATOR + exeName;
        if (FileUtil.checkIfExists(possible_path)) {
          return possible_path;
        }
      }
    }
    return null;
  }

  @Nullable
  public static String findRubyInterpreterInPATH(@NotNull final Map<String, String> buildParameters) {
    if (SystemInfo.isWindows) {
      //ruby.exe file
      String path = findExecutableByNameInPATH(RUBY_EXE_WIN, buildParameters);
      if (path != null) {
        return path;
      }
      //ruby.bat file
      path = findExecutableByNameInPATH(RUBY_EXE_WIN_BAT, buildParameters);
      if (path != null) {
        return path;
      }
      //jruby.exe file
      path = findExecutableByNameInPATH(JRUBY_EXE_WIN, buildParameters);
      if (path != null) {
        return path;
      }
      //jruby.bat file
      return findExecutableByNameInPATH(JRUBY_EXE_WIN_BAT, buildParameters);
    } else if (SystemInfo.isUnix) {
      //ruby file
      String path = findExecutableByNameInPATH(RUBY_EXE_UNIX, buildParameters);
      if (path != null) {
        return path;
      }
      //jruby file
      return findExecutableByNameInPATH(JRUBY_EXE_UNIX, buildParameters);
    } else {
      throw new RuntimeException(RakeRunnerBundle.MSG_OS_NOT_SUPPORTED);
    }
  }

  @Nullable
  public static String getUserHomeFolder() {
    final String home = System.getProperty("user.home");
    return FileUtil.checkIfDirExists(home) ? home : null;
  }
}
