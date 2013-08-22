package jetbrains.buildServer.agent.ruby.rvm.util;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
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
    final List<String> list = CollectionsUtil.filterCollection(Arrays.asList(stdout.split("\n")), new Filter<String>() {
      public boolean accept(@NotNull final String data) {
        final String trim = data.trim();
        return !trim.isEmpty() && !trim.startsWith("#");
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
