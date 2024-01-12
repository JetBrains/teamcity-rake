

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;
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

  public static final String RUBY_EXE_WIN = "ruby.exe";
  public static final String RUBY_EXE_WIN_BAT = "ruby.bat";
  public static final String RUBY_EXE_UNIX = "ruby";
  public static final String JRUBY_EXE_WIN = "jruby.exe";
  public static final String JRUBY_EXE_WIN_BAT = "jruby.bat";
  public static final String JRUBY_EXE_UNIX = "jruby";

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
  public static String getPATHEnvVariable(@NotNull final Map<String, String> envVariables) {
    return envVariables.get(getPATHEnvVariableKey());
  }

  @NotNull
  public static String getPATHEnvVariableKey() {
    return ENVIRONMENT_PATH_VARIABLE_NAME;
  }

  @Nullable
  public static String findExecutableByNameInPATH(@NotNull final String exeName,
                                                  @NotNull final Map<String, String> envVariables) {
    final String path = getPATHEnvVariable(envVariables);
    if (path != null) {
      final StringTokenizer st = new StringTokenizer(path, File.pathSeparator);

      //tokens - are pathes with system-dependent slashes
      while (st.hasMoreTokens()) {
        final String possible_path = st.nextToken() + INDEPENDENT_PATH_SEPARATOR + exeName;
        if (FileUtil2.checkIfExists(possible_path)) {
          return possible_path;
        }
      }
    }
    return null;
  }

  @Nullable
  public static String findRubyInterpreterInPATH(@NotNull final Map<String, String> envVariables) {
    if (SystemInfo.isWindows) {
      //ruby.exe file
      String path = findExecutableByNameInPATH(RUBY_EXE_WIN, envVariables);
      if (path != null) {
        return path;
      }
      //ruby.bat file
      path = findExecutableByNameInPATH(RUBY_EXE_WIN_BAT, envVariables);
      if (path != null) {
        return path;
      }
      //jruby.exe file
      path = findExecutableByNameInPATH(JRUBY_EXE_WIN, envVariables);
      if (path != null) {
        return path;
      }
      //jruby.bat file
      return findExecutableByNameInPATH(JRUBY_EXE_WIN_BAT, envVariables);
    } else if (SystemInfo.isUnix) {
      //ruby file
      String path = findExecutableByNameInPATH(RUBY_EXE_UNIX, envVariables);
      if (path != null) {
        return path;
      }
      //jruby file
      return findExecutableByNameInPATH(JRUBY_EXE_UNIX, envVariables);
    } else {
      throw new RuntimeException(RakeRunnerBundle.MSG_OS_NOT_SUPPORTED);
    }
  }

  @Nullable
  public static String getUserHomeFolder() {
    final String home = System.getProperty("user.home");
    return FileUtil2.checkIfDirExists(home) ? home : null;
  }
}