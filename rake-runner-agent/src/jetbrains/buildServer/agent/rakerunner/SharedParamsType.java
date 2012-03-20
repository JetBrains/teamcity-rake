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

package jetbrains.buildServer.agent.rakerunner;

import java.io.File;
import java.util.Map;
import jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil;
import jetbrains.buildServer.agent.ruby.RubySdk;
import jetbrains.buildServer.agent.ruby.impl.RubySdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRCBasedRubySdkImpl;
import jetbrains.buildServer.agent.ruby.rvm.impl.RVMRubySdkImpl;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rvm.RVMSupportUtil;

import static jetbrains.buildServer.agent.rakerunner.utils.InternalRubySdkUtil.findSystemInterpreterPath;
import static org.jetbrains.plugins.ruby.rvm.RVMSupportUtil.RVM_SYSTEM_INTERPRETER;

/**
 * @author Vladislav.Rassokhin
 */
public enum SharedParamsType {
  NOT_SETTED("notsetted") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final Map<String, String> buildParameters,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      return DEFAULT.createSdk(buildParameters, sharedParams);
    }
  },
  DEFAULT("default") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final Map<String, String> buildParameters,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      return new RubySdkImpl(findSystemInterpreterPath(buildParameters), true);
    }
  },
  INTERPRETER_PATH("intpath") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final Map<String, String> buildParameters,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String path = StringUtil.emptyIfNull(sharedParams.getInterpreterPath());
      InternalRubySdkUtil.checkInterpreterPathValid(path);
      return new RubySdkImpl(path, false);
    }
  },
  RVM("rvmsdk") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final Map<String, String> buildParameters,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String sdkName = StringUtil.emptyIfNull(sharedParams.getRVMSdkName());

      // at first lets check that it isn't "system" interpreter
      if (RVMSupportUtil.isSystemRuby(sdkName)) {
        return new RVMRubySdkImpl(findSystemInterpreterPath(buildParameters), RVM_SYSTEM_INTERPRETER, true, null);
      }

      // build  dist/gemsets table, match ref with dist. name
      final String gemset = sharedParams.getRVMGemsetName();
      final String suitableSdk = RVMSupportUtil.determineSuitableRVMSdkDist(sdkName, gemset);

      if (suitableSdk != null) {
        return new RVMRubySdkImpl(RVMSupportUtil.suggestInterpretatorPath(suitableSdk), suitableSdk, false, gemset);
      }
      final String msg = "Gemset '" + gemset + "' isn't defined for Ruby interpreter '" + sdkName
                         + "' or the interpreter doesn't exist or isn't a file or isn't a valid RVM interpreter name.";
      throw new RakeTasksBuildService.MyBuildFailureException(msg);
    }
  },
  RVMRC("rvmrc") {
    @NotNull
    @Override
    public RubySdk createSdk(@NotNull final Map<String, String> buildParameters,
                             @NotNull final SharedParams sharedParams)
      throws RakeTasksBuildService.MyBuildFailureException {
      final String rvmrc = StringUtil.emptyIfNull(sharedParams.getRVMRCPath());

      final File file;
      if (!StringUtil.isEmptyOrSpaces(rvmrc) && FileUtil.isAbsolute(rvmrc)) {
        file = new File(rvmrc + "/.rvmrc");
      } else {
        final String checkoutDir = jetbrains.buildServer.agent.rakerunner.utils.FileUtil.getCheckoutDirectoryPath(buildParameters);
        file = new File(checkoutDir, rvmrc + "/.rvmrc");
      }
      if (!jetbrains.buildServer.agent.rakerunner.utils.FileUtil.checkIfFileExists(file)) {
        throw new RakeTasksBuildService.MyBuildFailureException(".rvmrc file not found. specified path: \"" + file.getPath() + "\"");
      }

      // Create SDK using known .rvmrc file
      return RVMRCBasedRubySdkImpl.createAndSetup(file.getParentFile().getAbsolutePath());
    }
  };

  private final String myValue;

  SharedParamsType(@NotNull final String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

  @NotNull
  public abstract RubySdk createSdk(@NotNull final Map<String, String> buildParameters,
                                    @NotNull final SharedParams sharedParams)
    throws RakeTasksBuildService.MyBuildFailureException;
}
