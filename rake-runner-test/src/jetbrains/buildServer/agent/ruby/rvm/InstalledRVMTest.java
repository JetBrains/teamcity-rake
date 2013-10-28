package jetbrains.buildServer.agent.ruby.rvm;

import jetbrains.buildServer.agent.ruby.rvm.util.RVMUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.slow.plugins.rakerunner.RakeRunnerTestUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static jetbrains.buildServer.agent.ruby.rvm.InstalledRVM.getDistrForNameFromMap;

/**
 * @author Vladislav.Rassokhin
 */
@Test(groups = "unix")
public class InstalledRVMTest {

  private Map<Pattern, String> myMap;

  private final Map<String, String> myCustomContents = new HashMap<String, String>();
  private InstalledRVM myMockedRVM;

  @BeforeClass
  public void beforeClass() throws Exception {
    myMap = RVMUtil.convertListKnownIntoResolvingMap(FileUtil.readText(RakeRunnerTestUtil.getTestDataItemPath("rvm_list_known.txt")));
  }

  @BeforeMethod
  public void beforeMethod() throws Exception {
    myCustomContents.clear();
    myMockedRVM = new InstalledRVM("", InstalledRVM.Type.Global) {
      @NotNull
      @Override
      public String executeCommandLine(@NotNull final String... query) {
        final List<String> args = Arrays.asList(query).subList(1, query.length);
        final String file = "rvm " + StringUtil.join(args, " ");
        if (myCustomContents.containsKey(file)) {
          return myCustomContents.get(file);
        }
        throw new IllegalStateException();
      }
    };
  }

  @Test(dataProvider = "resolvings")
  public void testResolving(final String from, final String to) throws Exception {
    Assert.assertNotNull(myMap, "Map should be initialized before this test");
    Assert.assertEquals(getDistrForNameFromMap(from, myMap), to);
  }

  @Test(dataProvider = "defaults")
  public void testGetDefaultInterpreter(String stdout, String expected) throws Exception {
    myCustomContents.put("rvm list default string", stdout);
    Assert.assertEquals(myMockedRVM.getDefualtInterpreter(), expected);
  }

  @Test(dataProvider = "installed")
  public void testGetInstalledRubies(String... args) throws Exception {
    myCustomContents.put("rvm list strings", args[0]);
    Assert.assertEquals(myMockedRVM.getInstalledRubies(), Arrays.asList(args).subList(1, args.length));
  }

  @DataProvider
  public static String[][] defaults() {
    return new String[][]{
      {"\n\n", null},
      {"", null},
      {"1.8.7", "1.8.7"},
      {"\n1.8.7\ndropped string\n", "1.8.7"},
      {"1.8.7\ndropped string\n", "1.8.7"},
      {"ruby-1.9.3-p448\n\n", "ruby-1.9.3-p448"},
      {"Warning! GARBAGE GARBAGE,\n" +
       "         GARBAGE: 'rvm use ruby-1.9.3-p448'.\n" +
       "basename: missing operand\n" +
       "Try 'basename --help' for more information.\n", null},
    };
  }

  @DataProvider
  public static String[][] installed() {
    return new String[][]{
      {"jruby-1.7.4\n" +
       "ruby-1.9.3-p448", "jruby-1.7.4", "ruby-1.9.3-p448"},

      {"Warning! PATH is not properly set up, '/home/vlad/.rvm/gems/ruby-1.9.3-p448/bin' is not at first place,\n" +
       "         usually this is caused by shell initialization files - check them for 'PATH=...' entries,\n" +
       "         it might also help to re-add RVM to your dotfiles: 'rvm get stable --auto-dotfiles',\n" +
       "         to fix temporarily in this shell session run: 'rvm use ruby-1.9.3-p448'.\n" +
       "jruby-1.6.8\n" +
       "ruby-1.9.2-p320       ", "jruby-1.6.8", "ruby-1.9.2-p320"},
    };
  }

  @DataProvider
  public static String[][] resolvings() {
    return new String[][]{
      {"1.8.7", "ruby-1.8.7-p374"},
      {"ruby-1.8.7", "ruby-1.8.7-p374"},
      {"1.8.7-p374", "ruby-1.8.7-p374"},
      {"ruby-1.8.7-p374", "ruby-1.8.7-p374"},
      {"1.9.2", "ruby-1.9.2-p320"},
      {"ruby-1.9.2", "ruby-1.9.2-p320"},
      {"1.9.3", "ruby-1.9.3-p448"},
      {"ruby-1.9.3", "ruby-1.9.3-p448"},
      {"jruby", "jruby-1.7.4"},
      {"jruby-1.7.4", "jruby-1.7.4"},
      {"jruby-head", "jruby-head"},
      {"rbx", "rbx-head"},
      {"rbx-head", "rbx-head"},
      {"ree", "ree-1.8.7-2012.02"},
      {"ree-1.8.7", "ree-1.8.7-2012.02"},
      {"ree-2012.02", "ree-1.8.7-2012.02"},
      {"ree-1.8.7-2012.02", "ree-1.8.7-2012.02"},
      {"macruby", "macruby-0.12"},
      {"macruby-0.12", "macruby-0.12"},
      {"maglev", "maglev-head"},
      {"maglev-head", "maglev-head"},
      {"ironruby", "ironruby-1.1.3"},
      {"ironruby-1.1.3", "ironruby-1.1.3"},
      //{"", ""},
    };
  }
}
