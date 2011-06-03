package jetbrains.slow.plugins.rakerunner;

import java.io.File;

/**
 * @author Roman.Chernyatchik
 */
public class RakeRunnerTestUtil {
  public static final String TESTDATA_PATH = "svnrepo/rake-runner/rake-runner-test/testData/";

  static File getTestDataItemPath(final String fileOrFolderRelativePath) {
    return new File(TESTDATA_PATH + fileOrFolderRelativePath);
  }
}
