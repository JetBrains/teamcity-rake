package jetbrains.slow.plugins.rakerunner;

import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Roman.Chernyatchik
 */
public class RakeRunnerTestUtil {
  public static final String TESTDATA_PATH = "svnrepo/rake-runner/rake-runner-test/testData/";
  public static final String INTERPRETER_PATH_PROPERTY = "rake-runner.ruby.interpreter.path";
  public static final String RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY = "rake-runner.testing.ruby.version";
  public static final String DEFAULT_GEMSET_NAME = "all-trunk";
  public static final String GEMSET_PREFIX = "";

  static File getTestDataItemPath(final String fileOrFolderRelativePath) {
    return new File(TESTDATA_PATH + fileOrFolderRelativePath);
  }

  static public void setInterpreterPath(@NotNull final BuildTypeSettings bt) {
    String interpreterPath = System.getProperty(INTERPRETER_PATH_PROPERTY);
    if (!StringUtil.isEmpty(interpreterPath)) {

      if (!new File(interpreterPath).exists() && interpreterPath.indexOf("jruby-1.3.0") > 0) {
        interpreterPath = interpreterPath.replace("jruby-1.3.0", "jruby-1.4.0");
      }

      bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH, interpreterPath));
      bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
          RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH.getModeValueString()));
    }
  }

  static public void setRVMConfiguration(@NotNull final BuildTypeSettings bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE,
        RakeRunnerUtils.RubyConfigMode.RVM.getModeValueString()));
    useRVMRubySDK(System.getProperty(RAKE_RUNNER_TESTING_RUBY_VERSION_PROPERTY), bt);
    useRVMGemSet(DEFAULT_GEMSET_NAME, bt);
  }

  static public void useRVMRubySDK(@NotNull String sdkname, @NotNull final BuildTypeSettings bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, sdkname));
  }

  static public void useRVMGemSet(@NotNull String gemset, @NotNull final BuildTypeSettings bt) {
    bt.addRunParameter(new SimpleParameter(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME, GEMSET_PREFIX + gemset));
  }
}
