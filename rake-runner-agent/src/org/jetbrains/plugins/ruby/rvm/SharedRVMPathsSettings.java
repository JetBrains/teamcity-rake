package org.jetbrains.plugins.ruby.rvm;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.jetbrains.plugins.ruby.rvm.SharedRVMUtil.Constants.RVM_RUBIES_FOLDER_NAME;

/**
 * @author Roman.Chernyatchik
 */
public abstract class SharedRVMPathsSettings {
  public static final String[] KNOW_RVM_HOME_PATHS = new String[] {"/usr/local/rvm", "/opt/local/rvm"};

  @Nullable
  public abstract String getRvmHomePath();

  @Nullable
  protected String determineNonLocalRvmPath(@NotNull final Function<String, String> envVarValueByKeyFun) {
    if (SystemInfo.isWindows) {
      // N/A
      throw new UnsupportedOperationException("RVM support isn't implemented for Windows");
    }

    // custom path defined by env variable
    final String customRvmPath = envVarValueByKeyFun.fun("rvm_path");
    if (!StringUtil.isEmpty(customRvmPath)) {
      return customRvmPath;
    }


    // Try to detect global rvm on dist
    // Known paths: /usr/local/rvm, /opt/local/rvm
    for (String knowRvmHomePath : KNOW_RVM_HOME_PATHS) {
      final String markerPath = knowRvmHomePath + "/"+ RVM_RUBIES_FOLDER_NAME;
      final File file = new File(markerPath);

      if (file.exists()) {
        // rvm installation detected!
        return knowRvmHomePath;
      }
      // continue search..
    }

    // No custom rvm was found
    return null;
  }
}
