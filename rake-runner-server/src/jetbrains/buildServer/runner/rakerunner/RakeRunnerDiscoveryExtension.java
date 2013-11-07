/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.runner.rakerunner;

import com.intellij.openapi.util.io.FileUtil;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.runner.BuildFileRunnerConstants;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Vladislav.Rassokhin
 * @since 8.1
 */
public class RakeRunnerDiscoveryExtension extends BreadthFirstRunnerDiscoveryExtension {
  private static final Set<String> KNOWN_RAKEFILE_NAMES = CollectionsUtil.setOf("Rakefile", "rakefile", "Rakefile.rb", "rakefile.rb");

  @NotNull
  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(@NotNull final Element dir, @NotNull final List<Element> files) {
    final ArrayList<DiscoveredObject> discovered = new ArrayList<DiscoveredObject>();
    final List<Element> rakefiles = new LinkedList<Element>();
    Element gemfile = null;
    for (Element file : files) {
      if (KNOWN_RAKEFILE_NAMES.contains(file.getName())) {
        rakefiles.add(file);
      } else if (file.getName().equalsIgnoreCase("Gemfile")) {
        gemfile = file;
      }
    }
    for (Element rakefile : rakefiles) { // For weird case of multiple rakefiles in same directory (with/without .rb, first letter case)
      final String description = RakeRunnerBundle.RUNNER_DISPLAY_NAME + " (" + rakefile.getFullName() + ")";
      final Map<String, String> params = CollectionsUtil.asMap(
        BuildFileRunnerConstants.BUILD_FILE_PATH_KEY, FileUtil.toSystemIndependentName(rakefile.getFullName())
      );
      if (gemfile != null) {
        params.put(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY, Boolean.TRUE.toString());
      }
      discovered.add(new DiscoveredObject(RakeRunnerConstants.RUNNER_TYPE, params));
    }
    return discovered;
  }
}
