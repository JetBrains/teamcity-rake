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

import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import jetbrains.buildServer.util.StringUtil;

/**
 * @author Roman.Chernyatchik
 */
public class TextUtil {
  private static final Pattern EOL_SPLIT_PATTERN = Pattern.compile(" *(\r|\n|\r\n)+ *");

  /**
   * Unions many strings in one
   *
   * @param strings Srtings to concat
   * @return The result of concantenation
   */
  public static String concat(final String... strings) {
    final StringBuilder result = new StringBuilder(strings[0]);
    for (int i = 1; i < strings.length; i++) {
      if (strings[i] != null && strings[i].length() > 0) {
        result.append(' ');
        result.append(strings[i]);
      }
    }
    return result.toString();
  }

  public static boolean isEmptyOrWhitespaced(final @Nullable String s) {
    return s == null || s.trim().length() == 0;
  }

  public static String removeNewLine(String s) {
    if (s.length() == 0) return s;
    if (s.charAt(s.length() - 1) == '\n')
      s = s.substring(0, s.length() - 1);
    if (s.charAt(s.length() - 1) == '\r')
      s = s.substring(0, s.length() - 1);
    return s;
  }

  /**
   * Splits string by lines.
   *
   * @param string String to split
   * @return array of strings
   */
  public static String[] splitByLines(final String string) {
    return EOL_SPLIT_PATTERN.split(string);
  }

  public static String stripDoubleQuoteAroundValue(@NotNull final String str) {
    String text = str;
    if (StringUtil.startsWithChar(text, '\"')) {
      text = text.substring(1);
    }
    if (StringUtil.endsWithChar(text, '\"')) {
      text = text.substring(0, text.length() - 1);
    }
    return text;
  }
}
