

package jetbrains.buildServer.agent.ruby.rvm.impl;

import java.io.File;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.RvmShellRunner;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellBasedRubyScriptRunner;
import jetbrains.buildServer.agent.ruby.RubyVersionManager;
import jetbrains.buildServer.agent.ruby.impl.RubySdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubySdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMPathsSettings;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRubySdkImpl extends RubySdkImpl implements RVMRubySdk {

  @Nullable
  private final String myGemset;
  @NotNull
  private final String myName;
  @NotNull
  private final ShellBasedRubyScriptRunner myRubyScriptRunner;


  public RVMRubySdkImpl(@NotNull final File home, @NotNull final String name, @Nullable final String gemset) {
    super(home, null);
    myName = name;
    myGemset = gemset;
    myRubyScriptRunner = new ShellBasedRubyScriptRunner(new RvmShellRunner(RVMPathsSettings.getRVMNullSafe()) {
      @NotNull
      @Override
      protected String[] createProcessArguments(@NotNull final String rvmShellEx,
                                                @NotNull final String workingDirectory,
                                                @NotNull final File scriptFile) {
        return new String[]{rvmShellEx, getName(), scriptFile.getAbsolutePath()};
      }
    });
  }
  public RVMRubySdkImpl(@NotNull final File executable) {
    super(executable, true);
    myName = RVMSupportUtil.RVM_SYSTEM_INTERPRETER;
    myGemset = null;
    myRubyScriptRunner = new ShellBasedRubyScriptRunner(new RvmShellRunner(RVMPathsSettings.getRVMNullSafe()) {
      @NotNull
      @Override
      protected String[] createProcessArguments(@NotNull final String rvmShellEx,
                                                @NotNull final String workingDirectory,
                                                @NotNull final File scriptFile) {
        return new String[]{rvmShellEx, getName(), scriptFile.getAbsolutePath()};
      }
    });
  }

  @NotNull
  @Override
  public RubyVersionManager getVersionManager() {
    return RVMPathsSettings.getRVMNullSafe();
  }

  @Nullable
  @Override
  public String getGemset() {
    return myGemset;
  }

  @NotNull
  @Override
  public String getName() {
    return myGemset == null
           ? myName
           : myName + RVMSupportUtil.getGemsetSeparator() + myGemset;
  }

  @NotNull
  @Override
  public RubyScriptRunner getScriptRunner() {
    return myRubyScriptRunner;
  }

}