/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

import com.intellij.util.PathUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import jetbrains.buildServer.RunBuildException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman.Chernyatchik
 * @date: 23.12.2007
 */
public class RubySourcesUtil {
    private static final Logger LOG = Logger.getInstance(RubySourcesUtil.class.getName());

    @NonNls
    private static final String AGENT_BUNDLE_JAR = "RakeRunnerAgent.jar";
    @NonNls
    private static final String RUBY_SOURCES_SUBDIR = "rb";
    @NonNls
    private static final String RUBY_SOURCES_RAKE_RUNNER = "rakerunner.rb";

    @NotNull
    private static String getRootPath() throws RunBuildException {
        final String jarPath = PathUtil.getJarPathForClass(RubySourcesUtil.class);

        final File rubySourcesDir;
        if (jarPath != null && jarPath.endsWith(AGENT_BUNDLE_JAR)) {
            rubySourcesDir = new File(jarPath.substring(0, jarPath.length() - AGENT_BUNDLE_JAR.length())
                    + RUBY_SOURCES_SUBDIR);
        } else {
            rubySourcesDir = new File(jarPath + File.separatorChar + RUBY_SOURCES_SUBDIR);
        }

        try {
            if (rubySourcesDir.exists() && rubySourcesDir.isDirectory()) {
                return rubySourcesDir.getPath();
            }
            throw new RunBuildException("Unable to find bundled ruby scripts folder("
                                        + rubySourcesDir.getPath()
                                        + "). Plugin is damaged.");
        } catch (Exception e) {
            throw new RunBuildException(e);
        }
    }

    @NotNull
    public static String getRakeRunnerPath() throws RunBuildException {
        final String path = getRootPath();
        return path + File.separatorChar + RUBY_SOURCES_RAKE_RUNNER;
    }
}
