package org.jetbrains.plugins.ruby.rvm;

import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public abstract class SharedRVMPathsSettings {
  /**
   * @deprecated use getRvm().getPath()
   */
  @Nullable
  public abstract String getRvmHomePath();

  @Nullable
  public abstract InstalledRVM getRVM();
}
