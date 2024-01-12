

package jetbrains.buildServer.agent.ruby.rvm;

import java.util.Arrays;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RVMCommandLineProcessorTest {
  @Test
  public void testFormatShellArguments() throws Exception {
    doFormatShellArgumentsTest("");
    doFormatShellArgumentsTest("\"\"", "");
    doFormatShellArgumentsTest("a b c", "a", "b", "c");
    doFormatShellArgumentsTest("a \"'b''\" \"with space\" \"with(parens)\"", "a", "'b''", "with space", "with(parens)");
    doFormatShellArgumentsTest("\"-Dec2.ami-manifest-path=(unknown)\"", "-Dec2.ami-manifest-path=(unknown)");
  }

  @Test
  public void testEscapeShellArgument() throws Exception {
    doEscapeShellArgumentTest("", "");
    doEscapeShellArgumentTest("abc", "abc");
    doEscapeShellArgumentTest("\"", "\\\"");
    doEscapeShellArgumentTest("$", "\\$");
    doEscapeShellArgumentTest("`", "\\`");
    doEscapeShellArgumentTest("\\", "\\\\");
    doEscapeShellArgumentTest("\n", "\\\n");
  }

  private void doFormatShellArgumentsTest(final String expected, final String... input) {
    StringBuilder builder = new StringBuilder();
    RVMCommandLineProcessor.doFormatShellArguments(builder, Arrays.asList(input));
    assertEquals(builder.toString(), expected);
  }

  private void doEscapeShellArgumentTest(final String input, final Object expected) {
    String actual = RVMCommandLineProcessor.escapeShellArgument(input);
    assertEquals(actual, expected);
  }

}