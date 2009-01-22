/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
import static jetbrains.buildServer.util.FileUtil.toSystemDependentName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class OSUtil {

  public static String INDEPENDENT_PATH_SEPARATOR = "/";

  private static String ENVIRONMENT_PATH_VARIABLE_NAME;

  static {
    if (SystemInfo.isWindows) {
      ENVIRONMENT_PATH_VARIABLE_NAME = "Path";
    } else if (SystemInfo.isUnix) {
      ENVIRONMENT_PATH_VARIABLE_NAME = "PATH";
    } else {
      throw new RuntimeException(RakeRunnerBundle.MSG_OS_NOT_SUPPORTED);
    }
  }
  private static String RUBY_EXE_WIN = "ruby.exe";
  private static String RUBY_EXE_WIN_BAT = "ruby.bat";
  private static String RUBY_EXE_UNIX = "ruby";
  private static String JRUBY_EXE_WIN = "jruby.exe";
  private static String JRUBY_EXE_WIN_BAT = "jruby.bat";
  private static String JRUBY_EXE_UNIX = "jruby";

  public static String appendToRUBYLIBEnvVariable(@NotNull final String additionalPath) {
    final String rubyLibVal = System.getenv(RakeRunnerConstants.RUBYLIB_ENVIRONMENT_VARIABLE);

    final String pathValue;
    if (TextUtil.isEmpty(rubyLibVal)) {
      pathValue = toSystemDependentName(additionalPath);
    } else {
      pathValue = rubyLibVal + File.pathSeparatorChar + toSystemDependentName(additionalPath);
    }

    return pathValue;
  }

  public static String getPATHEnvVariable(@NotNull final Map<String, String> buildParameters) {
    return buildParameters.get(Constants.ENV_PREFIX + ENVIRONMENT_PATH_VARIABLE_NAME);
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
}
