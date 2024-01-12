

package jetbrains.buildServer.agent.ruby.rvm.util;

import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.slow.plugins.rakerunner.RakeRunnerTestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMUtilTest {
  @Test
  public void testConvertListKnownIntoResolvingMap() throws Exception {
    Map<Pattern, String> map = RVMUtil.convertListKnownIntoResolvingMap(FileUtil.readText(RakeRunnerTestUtil.getTestDataItemPath("rvm_list_known.txt")));
    for (Map.Entry<Pattern, String> entry : map.entrySet()) {
      System.out.println(entry.getKey().pattern() + " => " + entry.getValue());
    }
    // TODO: tests
    assertFalse(map.isEmpty());
    //assertEquals(map.size(), 43);
    for (Map.Entry<Pattern, String> entry : map.entrySet()) {
      assertTrue(entry.getKey().matcher(entry.getValue()).matches());
    }
  }

  @Test(dataProvider = "regex")
  public void testConvertRVMRegexToRegexAndFullName(String from, String expectedPattern, String expectedName) throws Exception {
    final Pair<String, String> pair = RVMUtil.convertRVMRegexToRegexAndFullName(from);
    assertEquals(pair.first, expectedPattern);
    assertEquals(pair.second, expectedName);
    assertTrue(pair.second.matches(pair.first));
  }

  @DataProvider
  public static String[][] regex() {
    return new String[][]{
      {"[ruby-]1.8.6[-p420]", "(\\Qruby-\\E)?\\Q1.8.6\\E(\\Q-p420\\E)?", "ruby-1.8.6-p420"},
      {"[ruby-]1.8.7[-p374]", "(\\Qruby-\\E)?\\Q1.8.7\\E(\\Q-p374\\E)?", "ruby-1.8.7-p374"},
      {"[ruby-]1.9.1[-p431]", "(\\Qruby-\\E)?\\Q1.9.1\\E(\\Q-p431\\E)?", "ruby-1.9.1-p431"},
      {"[ruby-]1.9.2[-p320]", "(\\Qruby-\\E)?\\Q1.9.2\\E(\\Q-p320\\E)?", "ruby-1.9.2-p320"},
      {"[ruby-]1.9.3[-p448]", "(\\Qruby-\\E)?\\Q1.9.3\\E(\\Q-p448\\E)?", "ruby-1.9.3-p448"},
      {"[ruby-]2.0.0[-p247]", "(\\Qruby-\\E)?\\Q2.0.0\\E(\\Q-p247\\E)?", "ruby-2.0.0-p247"},
      {"[ruby-]2.0.0-p195", "(\\Qruby-\\E)?\\Q2.0.0-p195\\E", "ruby-2.0.0-p195"},
      {"jruby-1.6.8", "\\Qjruby-1.6.8\\E", "jruby-1.6.8"},
    };
  }
}