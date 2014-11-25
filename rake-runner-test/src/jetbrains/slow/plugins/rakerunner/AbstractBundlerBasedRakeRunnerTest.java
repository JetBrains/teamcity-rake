/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.agent.rakerunner.utils.OSUtil;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.*;

/**
 * @author Vladislav.Rassokhin
 */
public abstract class AbstractBundlerBasedRakeRunnerTest extends AbstractRakeRunnerTest {
  private static final Logger LOG;

  static {
    LOG = Logger.getLogger(AbstractBundlerBasedRakeRunnerTest.class);
    LOG.setLevel(Level.DEBUG);
  }

  private final String myBundlerGemfileName;
  private final String myRVMGemsetName;
  private File myWorkingDirectory;
  private File myGemfile;

  protected AbstractBundlerBasedRakeRunnerTest(@NotNull final String ruby, @NotNull final String gemfileName) {
    setRubyVersion(ruby);
    myBundlerGemfileName = gemfileName;
    myRVMGemsetName = gemfileName;
  }

  @BeforeClass
  protected final void doPrepareWorkingDir() throws Throwable {
    myWorkingDirectory = FileUtil.createTempDirectory(getTestDataApp(), "", getTempsContainerDir());
    myWorkingDirectory.deleteOnExit();
    // Copy Gemfile
    myGemfile = copyGemfileToWorkingDirectory(myWorkingDirectory, myBundlerGemfileName);


    // Copy test data
    final File source = getTestDataPath(getTestDataApp());
    FileUtil.copyDir(source, myWorkingDirectory);
  }

  private File copyGemfileToWorkingDirectory(@NotNull final File workingDirectory, @NotNull final String gemset) throws java.io.IOException {
    final File gemfile = new File(workingDirectory, "Gemfile");
    FileUtil.copy(new File(new File(getTestDataPath("gems"), gemset), "Gemfile"), gemfile);
    return gemfile;
  }

  @BeforeClass(dependsOnMethods = {"doPrepareWorkingDir"})
  protected final void doPrepareEnvironment() throws Throwable {
    if (SystemInfo.isUnix) {
      doPrepareGemset(getRubyVersion(), myRVMGemsetName, LOG, myGemfile);
      if (isUseRVM()) gemsetToDelete.add(getRubyVersion() + "@" + myRVMGemsetName);
    } else if (SystemInfo.isWindows) {
      final File interpreter;
      try {
        interpreter = RakeRunnerTestUtil.getWindowsInterpreterExecutableFile(getRubyVersion());
        final File bin = interpreter.getParentFile();
        try {
          final Map<String, String> env = new HashMap<String, String>(System.getenv());
          OSUtil.prependToPATHEnvVariable(bin.getAbsolutePath(), env);
          RunCommandsHelper.runExecutable(LOG, new File(bin, "gem.bat").getAbsolutePath(), myWorkingDirectory, env, "install", "bundler");
          RunCommandsHelper.runExecutable(LOG, new File(bin, "bundle.bat").getAbsolutePath(), myWorkingDirectory, env, "install");
        } catch (Throwable e) {
          LOG.error("Failled to prepare environment: " + e.getMessage(), e);
        }
      } catch (RakeRunnerTestUtil.InterpreterNotFoundException e) {
        LOG.error("Failled to prepare environment: " + e.getMessage(), e);
      }
    }
  }

  private static final Set<String> gemsetToDelete = new HashSet<String>();

  @AfterSuite(alwaysRun = true, enabled = false)
  public void removeGemsets() throws Throwable {
    if (SystemInfo.isUnix && isUseRVM()) {
      for (String gemset : gemsetToDelete) {
        RunCommandsHelper.runBashScript(LOG, myWorkingDirectory,
            "source " + getTestDataPath("gems/checkRVMCommand.sh").getAbsolutePath(),
            "checkRVMCommand",
            String.format("rvm gemset delete \"%s\" --force", gemset)
        );
      }
    }
  }

  /**
   * Declared final because bug in TestNG.
   */
  @BeforeMethod
  @Override
  protected final void setUp1() throws Throwable {
    super.setUp1();
    setMessagesTranslationEnabled(true);
    useRVMGemSet(getRVMGemsetName());
    setUseBundle(true);
    getBuildType().addBuildParameter(new SimpleParameter(RakeRunnerConstants.CUSTOM_GEMFILE_RELATIVE_PATH, myGemfile.getAbsolutePath()));
    beforeMethod2();
  }

  @NotNull
  protected abstract String getTestDataApp();

  @NotNull
  protected String getRVMGemsetName() {
    return myRVMGemsetName;
  }

  /**
   * Use instead of setUp1
   */
  protected void beforeMethod2() throws Throwable {
  }

  protected void initAndDoTest(@NotNull final String taskName, final boolean shouldPass) throws Throwable {
    initAndDoTest(taskName, "", shouldPass, getTestDataApp(), myWorkingDirectory);
  }

  protected void initAndDoTest(@NotNull final String taskName, @Nullable final String resultFileSuffix, final boolean shouldPass)
      throws Throwable {
    initAndDoTest(taskName, resultFileSuffix, shouldPass, getTestDataApp(), myWorkingDirectory);
  }

  protected void initAndDoRealTest(@NotNull final String taskName, final boolean shouldPass) throws Throwable {
    initAndDoTest(taskName, "_real", shouldPass, getTestDataApp(), myWorkingDirectory);
  }

  protected void doTestWithoutLogCheck(@NotNull final String task_full_name, final boolean shouldPass) throws Throwable {
    initAndDoTest(task_full_name, null, shouldPass, getTestDataApp(), myWorkingDirectory);
  }

  @NotNull
  @Override
  protected List<String> getTestNameParametersList() {
    final List<String> list = super.getTestNameParametersList();
    list.add(myBundlerGemfileName);
    return list;
  }
}
