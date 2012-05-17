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

package jetbrains.buildServer.agent.ruby.rvm.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RubySDKUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RubyScriptRunner;
import jetbrains.buildServer.agent.rakerunner.utils.ShellScriptRunner;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import jetbrains.buildServer.agent.ruby.rvm.RVMRCBasedRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.util.RVMInfoUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import java.util.Map;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRCBasedRubySdkImpl extends RVMRubySdkImpl implements RVMRCBasedRubySdk {

  private static final Logger LOG = Logger.getInstance(RVMRCBasedRubySdkImpl.class.getName());

  public static RVMRCBasedRubySdkImpl createAndSetup(@NotNull final String pathToRVMRCFolder) {
    final RVMInfo info = RVMInfoUtil.gatherInfoUnderRvmShell(pathToRVMRCFolder);

    // Constructor params
    final String gemset;
    final String interpreterPath;
    final boolean isSystem;
    final String name;

    // Additional params
    final boolean isJRuby;
    final boolean isRuby19;


    interpreterPath = info.getSection(RVMInfo.Section.binaries).get("ruby");
    gemset = info.getSection(RVMInfo.Section.environment).get("gemset");
    name = info.getInterpreterName();


    LOG.debug("Interpreter Path is " + interpreterPath);
    LOG.debug("Gemset is " + gemset);
    LOG.debug("Name is " + name);

    if (RVMSupportUtil.RVM_SYSTEM_INTERPRETER.equals(name)) {
      isSystem = true;
      isJRuby = ShellScriptRunner.runUnderRvmShell(pathToRVMRCFolder, RubyScriptRunner.rubyScriptToShellScript(InternalRubySdkUtil.RUBY_PLATFORM_SCRIPT))
          .getStdout().contains("java");
      isRuby19 = ShellScriptRunner.runUnderRvmShell(pathToRVMRCFolder, RubyScriptRunner.rubyScriptToShellScript(InternalRubySdkUtil.RUBY_VERSION_SCRIPT))
          .getStdout().contains("1.9.");
    } else {
      isSystem = false;
      isJRuby = "jruby".equalsIgnoreCase(info.getSection(RVMInfo.Section.ruby).get("interpreter"));
      if (isJRuby) {
        isRuby19 = info.getSection(RVMInfo.Section.ruby).get("full_version").contains("ruby-1.9.");
      } else {
        isRuby19 = info.getSection(RVMInfo.Section.ruby).get("version").startsWith("1.9.");
      }
    }

    LOG.debug("IsSystem = " + isSystem);
    LOG.debug("IsRuby19 = " + isRuby19);
    LOG.debug("IsJRuby = " + isJRuby);

    final RVMRCBasedRubySdkImpl sdk = new RVMRCBasedRubySdkImpl(interpreterPath, name, isSystem, gemset);

    sdk.setIsJRuby(isJRuby);
    sdk.setIsRuby19(isRuby19);

    sdk.setLoadPathsLog(ShellScriptRunner.runUnderRvmShell(pathToRVMRCFolder, RubyScriptRunner.rubyScriptToShellScript(InternalRubySdkUtil.GET_LOAD_PATH_SCRIPT,
        // filter gem paths in case of Ruby 1.9 (use --disable-gems)
        isRuby19 ? new String[]{InternalRubySdkUtil.RUBY19_DISABLE_GEMS_OPTION} : new String[]{})));

    sdk.setGemPathsLog(ShellScriptRunner.runUnderRvmShell(pathToRVMRCFolder, RubyScriptRunner.rubyScriptToShellScript(RubySDKUtil.GET_GEM_PATHS_SCRIPT)));


    sdk.setIsSetupCompleted(true);

    return sdk;
  }

  private RVMRCBasedRubySdkImpl(@NotNull final String interpreterPath, final String name, final boolean system, final String gemset) {
    super(interpreterPath, name, system, gemset);
  }

  @Override
  public void setup(@NotNull final Map<String, String> buildConfEnvironment) {
    // Nothing to do, because setupped when constructed
  }
}
