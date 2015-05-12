/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.agent.rakerunner.utils.BundlerUtil;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Vladislav Rassokhin (vlad.rassokhin@gmail.com)
 */
@TestFor(testForClass = BundlerUtil.class)
public class BundlerUtilTest extends AbstractRakeRunnerTest {
  private static final Logger LOG;

  static {
    LOG = Logger.getLogger(BundlerUtilTest.class);
    LOG.setLevel(Level.DEBUG);
  }

  private File myCheckoutDirectory;
  private File wd1;
  private File wd2;

  @BeforeClass
  protected final void doPrepareWorkingDir() throws Throwable {
    myCheckoutDirectory = FileUtil.createTempDirectory(getTestDataApp(), "", getTempsContainerDir()).getAbsoluteFile();
    myCheckoutDirectory.deleteOnExit();


    // Copy test data
    final File source = getTestDataPath(getTestDataApp());
    FileUtil.copyDir(source, myCheckoutDirectory);

    wd1 = new File(myCheckoutDirectory, "wd1");
    copyGemFileAndRakefile(wd1);

    wd2 = new File(myCheckoutDirectory, "wd2");
    copyGemFileAndRakefile(wd2);
  }

  private String getTestDataApp() {
    return "bundler_checkout_test";
  }

  /**
   * Declared final because bug in TestNG.
   */
  @BeforeMethod
  @Override
  protected final void setUp1() throws Throwable {
    super.setUp1();
    useRVMGemSet("");
    setUseBundle(true);
    setPartialMessagesChecker();
    getBuildType().setCheckoutDirectory(myCheckoutDirectory.getAbsolutePath());
    doInstallBundlerGem(LOG);
  }


  @Test
  @TestFor(issues = "TW-31718")
  public void testDetermineGemfilePath() throws Throwable {
    initAndDoTest("check", "_cd", true, getTestDataApp(), myCheckoutDirectory);
    initAndDoTest("check", "_wd1", true, getTestDataApp(), wd1);
    initAndDoTest("check", "_wd2", true, getTestDataApp(), wd2);
  }
  @Test

  @TestFor(issues = "TW-31718")
  public void testDetermineGemfilePathOldStyle() throws Throwable {
    getBuildType().addBuildParameter(new SimpleParameter(RakeRunnerConstants.GEMFILE_RESOLVE_IN_CHECKOUT_DIRECTORY, "true"));
    initAndDoTest("check", "_cd", true, getTestDataApp(), myCheckoutDirectory);
    initAndDoTest("check", "_old_wd1", true, getTestDataApp(), wd1);
    initAndDoTest("check", "_old_wd2", true, getTestDataApp(), wd2);
  }

  private void copyGemFileAndRakefile(final File to) throws IOException {
    final File source = getTestDataPath(getTestDataApp());
    FileUtil.copy(new File(source, "Gemfile"), new File(to, "Gemfile"));
    FileUtil.copy(new File(source, "Rakefile"), new File(to, "Rakefile"));
  }
}
