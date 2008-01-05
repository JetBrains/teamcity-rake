/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */

package jetbrains.buildServer.agent.rakerunner.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman.Chernyatchik
 * @date: 05.01.2008
 */
public class FileUtil {
    /**
     * @param path Path to check
     * @return true, if path exists
     */
    public static boolean checkIfDirExists(@NotNull final String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }
}
