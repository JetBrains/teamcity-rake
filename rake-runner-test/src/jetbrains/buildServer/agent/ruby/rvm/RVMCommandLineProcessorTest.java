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