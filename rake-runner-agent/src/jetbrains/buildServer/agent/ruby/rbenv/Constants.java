/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.agent.ruby.rbenv;

/**
 * @author Vladislav.Rassokhin
 */
public interface Constants {
  static final String CONF_PARAMETER_PREFIX = "rbenv.";
  static final String CONF_RBENV_RUBIES_LIST = CONF_PARAMETER_PREFIX + "versions.list";
  static final String RBENV_ROOT_ENV_VARIABLE = "RBENV_ROOT";
  static final String RBENV_VERSION_ENV_VARIABLE = "RBENV_VERSION";
}
