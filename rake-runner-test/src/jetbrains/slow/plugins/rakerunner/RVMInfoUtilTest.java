package jetbrains.slow.plugins.rakerunner;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ScriptingFactory;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.RunnerUtil;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import jetbrains.buildServer.agent.ruby.rvm.util.RVMInfoUtil;
import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

/**
 * @author Vladislav.Rassokhin
 */
@TestFor(testForClass = {RVMInfoUtil.class})
@Test(groups = {"unix"})
public class RVMInfoUtilTest extends TestCase {
  @Override
  public void tearDown() throws Exception {
    ScriptingFactory.setDefault(ScriptingFactory.RVM_SHELL_BASED_SCRIPTING_FACTORY);
  }

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
    setupScriptingFactory(myCurrentName, infoMap);

    final RVMInfo info = RVMInfoUtil.gatherInfoUnderRvmShell("any");
    Assert.assertEquals("ruby-1.9.3-p194", info.getInterpreterName());
    for (RVMInfo.Section section : RVMInfo.Section.values()) {
      Assert.assertNotNull("section '" + section + "' doesn't exist", info.getSection(section));
    }
  }

  private void setupScriptingFactory(@NotNull final String currentOutput, @NotNull final Map<String, String> infoMap) {
    ScriptingFactory.setDefault(new ScriptingFactory() {

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
          public RunnerUtil.Output run(@NotNull final String script, @NotNull final String workingDirectory) {
            final String[] strings = script.split(" ");
            Assert.assertTrue(strings.length >= 2);
            Assert.assertEquals("Must starts with 'rvm'", "rvm", strings[0]);
            if ("current".equals(strings[1])) {
              return new RunnerUtil.Output(currentOutput, "");
            } else if ("info".equals(strings[1])) {
              final String type = strings[2];
              Assert.assertNotNull(type);
              final String ret = infoMap.get(type);
              Assert.assertNotNull("Not found: " + type, ret);
              return new RunnerUtil.Output(ret, "");
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
