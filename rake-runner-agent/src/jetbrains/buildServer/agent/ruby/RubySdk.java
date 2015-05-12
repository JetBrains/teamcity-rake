/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby;

import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.RubyScriptRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public interface RubySdk {
  @Nullable
  File getHome();

  @NotNull
  File getRubyExecutable();

  @Nullable
  RubyVersionManager getVersionManager();

  @Nullable
  String getGemset();

  @NotNull
  String getName();

  @Nullable
  String getVersion();

  boolean isSystem();

  boolean isRuby19();

  boolean isJRuby();

  @NotNull
  String[] getGemPaths();

  @NotNull
  String[] getLoadPath();

  void setup(@NotNull final Map<String, String> env);

  @NotNull
  ExecResult getGemPathsFetchLog();

  @NotNull
  ExecResult getLoadPathsFetchLog();

  @NotNull
  RubyScriptRunner getScriptRunner();
}
