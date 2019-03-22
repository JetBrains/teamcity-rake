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

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.rakerunner.RakeTasksBuildService;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class FileUtil2 {
  /**
   * @param path Path to check
   * @return true, if path exists and is directory
   */
  public static boolean checkIfDirExists(@NotNull final String path) {
    final File file = new File(path);
    return file.exists() && file.isDirectory();
  }

  /**
   * @param file file to check
   * @return true, if file exists and is normal file
   */
  public static boolean checkIfFileExists(@NotNull final File file) {
    return file.exists() && file.isFile();
  }

  /**
   * @param path Path to check
   * @return true, if path exists
   */
  public static boolean checkIfExists(@NotNull final String path) {
    return new File(path).exists();
  }

  @NotNull
  public static String getCanonicalPath(@NotNull final File file) throws RunBuildException {
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      throw new RunBuildException(e.getMessage(), e);
    } catch (SecurityException e) {
      throw new RunBuildException(e.getMessage(), e);
    }
  }

  @NotNull
  public static String getCanonicalPath2(@NotNull final File file) throws RakeTasksBuildService.MyBuildFailureException {
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      throw new RakeTasksBuildService.MyBuildFailureException(e.getMessage(), e);
    } catch (SecurityException e) {
      throw new RakeTasksBuildService.MyBuildFailureException(e.getMessage(), e);
    }
  }

  public static File getFirstExistChild(File directory, String... names) {
    if (!directory.isDirectory() || !directory.exists()) {
      return null;
    }
    for (String name : names) {
      final File file = new File(directory, name);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }
}
