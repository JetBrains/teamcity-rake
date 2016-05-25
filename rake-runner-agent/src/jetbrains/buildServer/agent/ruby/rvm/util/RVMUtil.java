/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby.rvm.util;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Vladislav.Rassokhin
 * @since 8.1
 */
public class RVMUtil {
  @NotNull
  public static Map<Pattern, String> convertListKnownIntoResolvingMap(@NotNull final String stdout) {
    final List<String> list = CollectionsUtil.convertAndFilterNulls(Arrays.asList(stdout.split("\n")), new Converter<String, String>() {
      public String createFrom(@NotNull final String source) {
        String trim = source.trim();
        final int comment = trim.indexOf('#');
        if (comment == 0) return null;
        if (comment > 0) trim = trim.substring(0, comment).trim();
        return !trim.isEmpty() && !trim.startsWith("#") && !trim.contains(" ") ? trim : null;
      }
    });

    final HashMap<Pattern, String> map = new LinkedHashMap<Pattern, String>();
    for (final String line : list) {
      final Pair<String, String> pair = convertRVMRegexToRegexAndFullName(line);

      map.put(Pattern.compile(pair.first), pair.second);
    }
    return map;
  }

  @NotNull
  public static Pair<String, String> convertRVMRegexToRegexAndFullName(@NotNull final String line) {
    final StringBuilder regexp = new StringBuilder();
    final StringBuilder natural = new StringBuilder();
    for (final StringTokenizer tokenizer = new StringTokenizer(line, "[]", true); tokenizer.hasMoreTokens(); ) {
      final String tok = tokenizer.nextToken();
      if (tok.equals("[")) {
        regexp.append("(");
      } else if (tok.equals("]")) {
        regexp.append(")?");
      } else {
        regexp.append(Pattern.quote(tok));
        natural.append(tok);
      }
    }
    return new Pair<String, String>(regexp.toString(), natural.toString());
  }
}
