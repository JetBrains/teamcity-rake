

package jetbrains.buildServer.agent.rakerunner;

import org.jetbrains.annotations.NonNls;

/**
 * @author Roman.Chernyatchik
 * @author Vladislav.Rassokhin
 */
public interface SharedRubyEnvSettings {
  @NonNls String SHARED_RUBY_PARAMS_TYPE = "teamcity.ruby.shared.params.type";
  @NonNls String SHARED_RUBY_PARAMS_ARE_APPLIED = "teamcity.ruby.shared.params.are.applied";
  @NonNls String SHARED_RUBY_INTERPRETER_PATH = "teamcity.ruby.shared.interpreter.path";
  @NonNls String SHARED_RUBY_RVM_SDK_NAME = "teamcity.ruby.shared.rvm.sdk.name";
  @NonNls String SHARED_RUBY_RVM_GEMSET_NAME = "teamcity.ruby.shared.rvm.gemset";
  @NonNls String SHARED_RUBY_RVM_GEMSET_CREATE = "teamcity.ruby.shared.rvm.gemset.create";
  @NonNls String SHARED_RUBY_RVM_RVMRC_PATH = "teamcity.ruby.shared.rvm.rvmrc.path";
  @NonNls String SHARED_RUBY_RVM_RUBY_VERSION_PATH = "teamcity.ruby.shared.rvm.ruby.version.path";
  @NonNls String SHARED_RUBY_RBENV_VERSION_NAME = "teamcity.ruby.shared.rbenv.version.name";
  @NonNls String SHARED_RUBY_RBENV_FILE_PATH = "teamcity.ruby.shared.rbenv.file.path";
}