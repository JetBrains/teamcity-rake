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

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.utils.FileUtil2;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import jetbrains.buildServer.agent.ruby.rbenv.Constants;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Vladislav.Rassokhin
 */
public class RbEnvDetectorForUNIX extends RbEnvDetector {
  public static final String[] KNOW_GLOBAL_RBENV_HOME_PATHS = new String[]{"/usr/local/rbenv"};

  @Override
  public InstalledRbEnv detect(@NotNull final Map<String, String> env) {
    // 1. Read "RBENV_ROOT" env variable
    // 2. Try to find local rbenv (RBENV_ROOT = $HOME/.rbenv)
    // 3. Try to find global rbenv (RBENV_ROOT = /usr/local/.rbenv)
    // NOTE: Steps 2 and 3 via 'which rbenv'
    // custom or system wide rbenv path

    @Nullable final String special = determinePathUsingEnvVariable(env);
    @Nullable final String global = determineGlobalInstallation();
    @Nullable final String local = determineLocalInstallation();

    if (special != null) {
      if (local != null && special.equals(local)) {
        return new InstalledRbEnv(local, InstalledRbEnv.Type.Local);
      } else if (global != null && special.equals(global)) {
        return new InstalledRbEnv(global, InstalledRbEnv.Type.Global);
      } else {
        return new InstalledRbEnv(special, InstalledRbEnv.Type.Special);
      }
    } else if (local != null) {
      return new InstalledRbEnv(local, InstalledRbEnv.Type.Local);
    } else if (global != null) {
      return new InstalledRbEnv(global, InstalledRbEnv.Type.Global);
    } else {
      // Nothing founded
      return null;
    }
  }

  @Nullable
  public static String determinePathUsingEnvVariable(@NotNull final Map<String, String> env) {
    // custom path defined by env variable
    final String path = env.get(Constants.RBENV_ROOT_ENV_VARIABLE);
    if (!StringUtil.isEmpty(path)) {
      return path;
    }

    // No special rbenv was found
    return null;
  }

  @Nullable
  public static String determineGlobalInstallation() {
    // Try to detect global rbenv on dist
    // Known paths: /usr/local/rbenv
    for (String knowHomePath : KNOW_GLOBAL_RBENV_HOME_PATHS) {
      final String exec = knowHomePath + "/bin/rbenv";

      try {
        if (FileUtil2.checkIfExists(exec)) {
          // rbenv installation detected!
          // Checking we have permissions
          if (InstalledRbEnv.executeCommandLine(exec, "help").contains("usage: rbenv")) {
            return knowHomePath;
          }
        }
      } catch (Exception ignored) {
      }
      // continue search..
    }

    // No global rbenv was found
    return null;
  }

  @Nullable
  public static String determineLocalInstallation() {
    final String home = OSUtil.getUserHomeFolder();
    // Cannot detect rbenv if $HOME directory unknowns.
    if (home == null) {
      return null;
    }

    final String path = home + "/" + ".rbenv";
    final String exec = path + "/bin/rbenv";
    if (FileUtil2.checkIfExists(exec)) {
      return path;
    }

    // No local rbenv was found
    return null;
  }
}
