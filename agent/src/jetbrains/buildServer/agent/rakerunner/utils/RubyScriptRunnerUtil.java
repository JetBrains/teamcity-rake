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

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 04.06.2007
 */
public class RubyScriptRunnerUtil {
    @NonNls private static final String BIN_DIR = "bin";
//    @NonNls private static final String WIN_RUBY_EXE =      "ruby.exe";
//    @NonNls private static final String LINUX_RUBY_EXE =    "ruby";
//    @NonNls private static final String MAC_RUBY_EXE =      "ruby";
//    @NonNls private static final String UNIX_RUBY_EXE =     "ruby";
//
//    @NonNls
//    private static String RUBY_EXE;
//
//    static {
//        if (SystemInfo.isWindows) {
//            RUBY_EXE = WIN_RUBY_EXE;
//        } else if (SystemInfo.isLinux) {
//            RUBY_EXE = LINUX_RUBY_EXE;
//        } else if (SystemInfo.isMac) {
//            RUBY_EXE = MAC_RUBY_EXE;
//        } else if (SystemInfo.isUnix) {
//            RUBY_EXE = UNIX_RUBY_EXE;
//        }
//    }
//
//
//    @NonNls private static final String EXE_PATH = BIN_DIR + "/" + RUBY_EXE;
    @NonNls private static final String RAKE = "rake";

    @NonNls private static final String BAT_SUFFIX = ".bat";
    @NonNls private static final String SH_SUFFIX = ".sh";


//    @NotNull
//    public static String getVMExecutablePath(@NotNull final String rubyHomePath) {
//        return normalizePath(rubyHomePath) + EXE_PATH;
//    }

//    @Nullable
//    public static String[] getRakeScriptCommand(@NotNull final String rubyHomePath) {
//        return getSystemScriptCommand(rubyHomePath, RAKE);
//    }

//    @Nullable
//    public static String[] getSystemScriptCommand(@NotNull final String rubyHomePath,
//                                                  @NotNull final String scriptName) {
//
//        final String homePath = normalizePath(rubyHomePath);
//
//        final String binFolder = homePath + BIN_DIR + "/";
//        if (checkIfPathExists(binFolder + scriptName)) {
//            return new String[]{getVMExecutablePath(rubyHomePath),
//                                binFolder + scriptName};
//        }
//        if (SystemInfo.isWindows
//                && checkIfPathExists(binFolder + scriptName + BAT_SUFFIX)) {
//            return new String[]{binFolder + scriptName + BAT_SUFFIX};
//        }
//        if (!SystemInfo.isWindows
//                && checkIfPathExists(binFolder + scriptName + SH_SUFFIX)) {
//            return new String[]{binFolder + scriptName + SH_SUFFIX};
//        }
//        return null;
//    }

    private static String normalizePath(final String rubyHomePath) {
        if (rubyHomePath.endsWith("/") || rubyHomePath.endsWith("\\")) {
            return rubyHomePath;
        }
        return rubyHomePath + "/";
    }

    /**
     * @param path Path to check
     * @return true, if path exists
     */
    private static boolean checkIfPathExists(@NotNull final String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }
}
