

package jetbrains.slow.plugins.rakerunner;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 * @author Vladislav.Rassokhin
 */
@Test
public class RSpecBuildLogTest extends AbstractRSpecTest {
  @Factory(dataProvider = "rspec", dataProviderClass = BundlerBasedTestsDataProvider.class)
  @TestWithGemfiles("rspec-trunk")
  public RSpecBuildLogTest(@NotNull final String ruby, @NotNull final String gemfile) {
    super(ruby, gemfile);
  }

  public void testSpecPassed() throws Throwable {
    doTestWithoutLogCheck("stat:passed", true);

    assertTestsCount(3, 0, 0);
  }

  public void testSpecFailed() throws Throwable {
    doTestWithoutLogCheck("stat:failed", false);

    assertTestsCount(0, 3, 0);
  }

  public void testSpecError() throws Throwable {
    doTestWithoutLogCheck("stat:error", false);

    assertTestsCount(0, 3, 0);
  }

  public void testSpecIgnored() throws Throwable {
    doTestWithoutLogCheck("stat:ignored", false);
    if (getRVMGemsetName().equals("rspec-2")) {
      assertTestsCount(0, 1, 2);
    } else {
      assertTestsCount(0, 1, 8);
    }
  }

  public void testSpecCompileError() throws Throwable {
    doTestWithoutLogCheck("stat:compile_error", false);

    assertTestsCount(0, 0, 0);
  }
}