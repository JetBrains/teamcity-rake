/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class FileUtil {
  /**
   * @param path Path to check
   * @return true, if path exists and is directory
   */
  public static boolean checkIfDirExists(@NotNull final String path) {
    final File file = new File(path);
    return file.exists() && file.isDirectory();
  }

  /**
   * @param path Path to check
   * @return true, if path exists
   */
  public static boolean checkIfExists(@NotNull final String path) {
    return new File(path).exists();
  }
}
