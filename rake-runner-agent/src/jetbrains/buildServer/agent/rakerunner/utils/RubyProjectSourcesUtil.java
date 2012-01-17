/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.PathUtil;
import jetbrains.buildServer.RunBuildException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
      // some old code, not sure that it is needed
      rubySourcesDir = new File("svnrepo/rake-runner/lib/rb");
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