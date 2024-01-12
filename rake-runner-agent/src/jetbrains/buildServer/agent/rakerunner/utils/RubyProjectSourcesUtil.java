

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.PathUtil;
import java.io.File;
import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class RubyProjectSourcesUtil {
  private static final Logger LOG = Logger.getInstance(RubyProjectSourcesUtil.class.getName());

  @NonNls
  private static final String RUBY_SOURCES_SUBDIR = "rb";
  @NonNls
  private static final String PATCH_FOLDER = File.separatorChar + "patch" + File.separatorChar;
  private static final String PATCH_FOLDER_BDD = PATCH_FOLDER + "bdd";
  private static final String PATCH_FOLDER_COMMON = PATCH_FOLDER + "common";
  private static final String PATCH_FOLDER_TESTUNIT = PATCH_FOLDER + "testunit";
  private static final String RUBY_SOURCES_RAKE_RUNNER = File.separatorChar + "runner" + File.separatorChar + "rakerunner.rb";
  @NonNls
  public static final String TUNIT_LOADPATH_PATH_SCRIPT = "test/unit/ui/teamcity/loadpath_patch.rb";

  @NotNull
  private static String getRootPath() throws RunBuildException {
    final String jarPath = PathUtil.getJarPathForClass(RubyProjectSourcesUtil.class);

    final File rubySourcesDir;
    if (jarPath != null && jarPath.endsWith(".jar")) {
      File jarFile = new File(jarPath);
      rubySourcesDir = new File(jarFile.getParentFile(), RUBY_SOURCES_SUBDIR);
    } else {
      // Used in tests
      File test = new File("lib/rb");
      if (!test.exists()) {
        test = new File("external-repos/rake-runner/lib/rb");
      }
      rubySourcesDir = test;
    }

    try {
      if (rubySourcesDir.exists() && rubySourcesDir.isDirectory()) {
        return rubySourcesDir.getCanonicalPath();
      }
      throw new RunBuildException("Unable to find bundled ruby scripts folder("
                                  + rubySourcesDir.getCanonicalPath()
                                  + " [original path: " + rubySourcesDir.getPath() + "]). Plugin is damaged.");
    } catch (Exception e) {
      throw new RunBuildException(e.getMessage(), e);
    }
  }

  @NotNull
  public static String getRakeRunnerPath() throws RunBuildException {
    return getRootPath() + RUBY_SOURCES_RAKE_RUNNER;
  }

  @NotNull
  public static String getLoadPath_PatchRoot_Bdd() throws RunBuildException {
    return getRootPath() + PATCH_FOLDER_BDD;
  }

  @NotNull
  public static String getLoadPath_PatchRoot_Common() throws RunBuildException {
    return getRootPath() + PATCH_FOLDER_COMMON;
  }

  @NotNull
  public static String getLoadPath_PatchRoot_TestUnit() throws RunBuildException {
    return getRootPath() + PATCH_FOLDER_TESTUNIT;
  }
}