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

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RubySDKUtil;
import jetbrains.buildServer.agent.rakerunner.utils.RubyScriptRunner;
import jetbrains.buildServer.agent.ruby.rvm.RVMRCBasedRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.util.RVMInfoUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMRCBasedRubySdkImpl extends RVMRubySdkImpl implements RVMRCBasedRubySdk {

  public static RVMRCBasedRubySdkImpl createAndSetup(@NotNull final String pathToRVMRCFolder) {
    final RubyScriptRunner.Output output = RVMInfoUtil.runRvmInfoUnderRvmShell(pathToRVMRCFolder);
    final List<Pair<String, String>> infos = RVMInfoUtil.parseOutput(output);

    // Constructor params
    final String gemset;
    String interpreterPath;
    final boolean isSystem;
    final String name;

    // Additional params
    final boolean isJRuby;
    final boolean isRuby19;


    interpreterPath = RVMInfoUtil.getInfoString(infos, "ruby");  // use which rvm instead
    gemset = RVMInfoUtil.getInfoString(infos, "gemset");


    final String my_ruby_home = RVMInfoUtil.getInfoString(infos, "MY_RUBY_HOME");
    if (StringUtil.isEmptyOrSpaces(my_ruby_home)) {
      isSystem = true;
      name = RVMSupportUtil.RVM_SYSTEM_INTERPRETER;
      isJRuby = RubyScriptRunner.runUnderRvmShell(pathToRVMRCFolder, "ruby", "-e", InternalRubySdkUtil.RUBY_PLATFORM_SCRIPT)
        .getStdout().contains("java");
      isRuby19 = RubyScriptRunner.runUnderRvmShell(pathToRVMRCFolder, "ruby", "-e", InternalRubySdkUtil.RUBY_VERSION_SCRIPT)
        .getStdout().contains("1.9.");
    } else {
      isSystem = false;
      name = new File(my_ruby_home).getName();
      isJRuby = name.startsWith("jruby");
      isRuby19 = name.contains("1.9.");
    }

    final RVMRCBasedRubySdkImpl sdk = new RVMRCBasedRubySdkImpl(interpreterPath, name, isSystem, gemset);

    sdk.setIsJRuby(isJRuby);
    sdk.setIsRuby19(isRuby19);

    {
      final RubyScriptRunner.Output output1;
      if (isRuby19) {
        // filter gem paths in case of Ruby 1.9 (use --disable-gems)
        output1 =
          RubyScriptRunner.runUnderRvmShell(pathToRVMRCFolder, "ruby", InternalRubySdkUtil.RUBY19_DISABLE_GEMS_OPTION, "-e",
                                            InternalRubySdkUtil.GET_LOAD_PATH_SCRIPT);
      } else {
        output1 = RubyScriptRunner.runUnderRvmShell(pathToRVMRCFolder, "ruby", "-e", InternalRubySdkUtil.GET_LOAD_PATH_SCRIPT);
      }
      sdk.setLoadPathsLog(output1);
    }
    sdk.setGemPathsLog(RubyScriptRunner.runUnderRvmShell(pathToRVMRCFolder, "ruby", "-e", RubySDKUtil.GET_GEM_PATHS_SCRIPT));


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
