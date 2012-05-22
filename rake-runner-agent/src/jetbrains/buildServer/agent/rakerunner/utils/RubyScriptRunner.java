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
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.agent.ruby.RubySdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

/**
 * @author Roman.Chernyatchik
 */
public class RubyScriptRunner {
  private static final Logger LOG = Logger.getInstance(RubyScriptRunner.class.getName());

  /**
   * Returns out after scriptSource run.
   *
   * @param sdk          Ruby SDK
   * @param scriptSource script source to tun
   * @param rubyArgs     ruby Arguments
   * @param scriptArgs   script arguments
   * @return Out object
   */
  @NotNull
  public static RunnerUtil.Output runScriptFromSource(@NotNull final RubySdk sdk,
                                                      @NotNull final String[] rubyArgs,
                                                      @NotNull final String scriptSource,
                                                      @NotNull final String[] scriptArgs,
                                                      @Nullable final Map<String, String> buildConfEnvironment) {
    RunnerUtil.Output result = null;
    File scriptFile = null;
    try {
      // Writing source to the temp file
      scriptFile = File.createTempFile("script", ".rb");
      PrintStream out = new PrintStream(scriptFile);
      out.print(scriptSource);
      out.close();

      //Args
      final String[] args = new String[2 + rubyArgs.length + scriptArgs.length];
      args[0] = sdk.getInterpreterPath();
      System.arraycopy(rubyArgs, 0, args, 1, rubyArgs.length);
      args[rubyArgs.length + 1] = scriptFile.getPath();
      System.arraycopy(scriptArgs, 0, args, rubyArgs.length + 2, scriptArgs.length);

      // Env
      final HashMap<String, String> processEnv = new HashMap<String, String>(buildConfEnvironment);
      RVMSupportUtil.patchEnvForRVMIfNecessary(sdk, processEnv);

      //Result
      result = RunnerUtil.run(null, processEnv, args);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      if (scriptFile != null && scriptFile.exists()) {
        scriptFile.delete();
      }
    }

    //noinspection ConstantConditions
    return result;
  }

}
