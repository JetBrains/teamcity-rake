/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ScriptingRunnersProvider;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import jetbrains.buildServer.agent.ruby.rvm.util.RVMInfoUtil;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladislav.Rassokhin
 */
@TestFor(testForClass = {RVMInfoUtil.class})
@Test(groups = {"unix"})
public class RVMInfoUtilTest {

  @Test(groups = {"unix"})
  public void testParsingOk() throws Exception {
    final String myCurrentName = "ruby-1.9.3-p194@rails";
    final Map<String, String> infoMap = new HashMap<String, String>();
    infoMap.put("system",
                "    uname:       \"Linux unit-421 3.2.16-gentoo #1 SMP Sat May 12 14:48:35 MSK 2012 x86_64 Intel(R) Core(TM) i7-2600 CPU @ 3.40GHz GenuineIntel GNU/Linux\"\n" +
                "    bash:        \"/bin/bash => GNU bash, version 4.2.20(1)-release (x86_64-pc-linux-gnu)\"\n" +
                "    zsh:         \"/bin/zsh => zsh 4.3.15 (x86_64-pc-linux-gnu)\"");
    infoMap.put("rvm",
                "    version:      \"rvm 1.13.8 (stable) by Wayne E. Seguin <wayneeseguin@gmail.com>, Michal Papis <mpapis@gmail.com> [https://rvm.io/]\"\n" +
                "    updated:      \"7 minutes 12 seconds ago\"\n");
    infoMap.put("ruby", "    interpreter:  \"ruby\"\n" +
                        "    version:      \"1.9.3p194\"\n" +
                        "    date:         \"2012-04-20\"\n" +
                        "    platform:     \"x86_64-linux\"\n" +
                        "    patchlevel:   \"2012-04-20 revision 35410\"\n" +
                        "    full_version: \"ruby 1.9.3p194 (2012-04-20 revision 35410) [x86_64-linux]\"\n");
    infoMap.put("homes", "    gem:          \"/home/vlad/.rvm/gems/ruby-1.9.3-p194@rails\"\n" +
                         "    ruby:         \"/home/vlad/.rvm/rubies/ruby-1.9.3-p194");
    infoMap.put("binaries", "    ruby:         \"/home/vlad/.rvm/rubies/ruby-1.9.3-p194/bin/ruby\"\n" +
                            "    irb:          \"/home/vlad/.rvm/rubies/ruby-1.9.3-p194/bin/irb\"\n" +
                            "    gem:          \"/home/vlad/.rvm/rubies/ruby-1.9.3-p194/bin/gem\"\n" +
                            "    rake:         \"/home/vlad/.rvm/gems/ruby-1.9.3-p194@rails/bin/rake\"\n");
    infoMap.put("environment",
                "    PATH:         \"/home/vlad/.rvm/gems/ruby-1.9.3-p194@rails/bin:/home/vlad/.rvm/gems/ruby-1.9.3-p194@global/bin:/home/vlad/.rvm/rubies/ruby-1.9.3-p194/bin:/home/vlad/.rvm/bin:/media/programs-fast/bin:/home/vlad/bin:/usr/local/bin:/usr/bin:/bin:/opt/bin:/usr/x86_64-pc-linux-gnu/gcc-bin/4.6.0:/usr/games/bin:/opt/ec2-api-tools/bin\"\n" +
                "    GEM_HOME:     \"/home/vlad/.rvm/gems/ruby-1.9.3-p194@rails\"\n" +
                "    GEM_PATH:     \"/home/vlad/.rvm/gems/ruby-1.9.3-p194@rails:/home/vlad/.rvm/gems/ruby-1.9.3-p194@global\"\n" +
                "    MY_RUBY_HOME: \"/home/vlad/.rvm/rubies/ruby-1.9.3-p194\"\n" +
                "    IRBRC:        \"/home/vlad/.rvm/rubies/ruby-1.9.3-p194/.irbrc\"\n" +
                "    RUBYOPT:      \"-rauto_gem\"\n" +
                "    gemset:       \"rails\"\n");
    try {
      setupScriptingFactory(myCurrentName, infoMap);

      final RVMInfo info = RVMInfoUtil.gatherInfoUnderRvmShell("any", null);
      Assert.assertEquals(info.getInterpreterName(), "ruby-1.9.3-p194");
      for (RVMInfo.Section section : RVMInfo.Section.values()) {
        Assert.assertNotNull(info.getSection(section), "section '" + section + "' doesn't exist");
      }
    } finally {
      ScriptingRunnersProvider.setRVMDefault(ScriptingRunnersProvider.RVM_SHELL_BASED_SCRIPTING_RUNNERS_PROVIDER);
    }
  }

  private void setupScriptingFactory(@NotNull final String currentOutput, @NotNull final Map<String, String> infoMap) {
    ScriptingRunnersProvider.setRVMDefault(new ScriptingRunnersProvider() {

      @NotNull
      @Override
      public RubyScriptRunner getRubyScriptRunner() {
        throw new UnsupportedOperationException("This is a mock for tests");
      }

      @NotNull
      @Override
      public ShellScriptRunner getShellScriptRunner() {
        return new ShellScriptRunner() {
          @NotNull
          public ExecResult run(@NotNull final String script,
                                @NotNull final String workingDirectory,
                                @Nullable final Map<String, String> environment) {
            final String[] strings = script.split(" ");
            Assert.assertTrue(strings.length >= 2);
            Assert.assertEquals(strings[0], "rvm", "Must starts with 'rvm'");
            if ("current".equals(strings[1])) {
              final ExecResult result = new ExecResult();
              result.setExitCode(0);
              result.setStdout(currentOutput);
              return result;
            } else if ("info".equals(strings[1])) {
              final String type = strings[2];
              Assert.assertNotNull(type);
              final String ret = infoMap.get(type);
              Assert.assertNotNull(ret, "Not found: " + type);
              final ExecResult result = new ExecResult();
              result.setExitCode(0);
              result.setStdout(ret);
              return result;
            } else {
              Assert.fail("Mock does not supports command '" + script + "'");
              return null;
            }
          }
        };
      }
    });
  }
}
