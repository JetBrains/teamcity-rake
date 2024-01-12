

package jetbrains.buildServer.feature;

import org.jetbrains.annotations.NonNls;

/**
 * @author Vladislav.Rassokhin
 */
public interface RubyEnvConfiguratorConstants {
  @NonNls String RUBY_ENV_CONFIGURATOR_FEATURE_TYPE = "ruby.env.configurator";

  @NonNls String UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY = "ui.ruby.configurator.fail.build.if.interpreter.not.found";
  @NonNls String UI_RVM_GEMSET_NAME_KEY = "ui.ruby.configurator.rvm.gemset.name";
  @NonNls String UI_RVM_GEMSET_CREATE_IF_NON_EXISTS = "ui.ruby.configurator.rvm.gemset.create.if.non.exists";
  @NonNls String UI_RVM_SDK_NAME_KEY = "ui.ruby.configurator.rvm.sdk.name";
  @NonNls String UI_RVM_RVMRC_PATH_KEY = "ui.ruby.configurator.rvm.rvmrc.path";
  @NonNls String UI_RVM_RUBY_VERSION_PATH_KEY = "ui.ruby.configurator.rvm.ruby_version.path";
  @NonNls String UI_USE_RVM_KEY = "ui.ruby.configurator.use.rvm";
  @NonNls String UI_RUBY_SDK_PATH_KEY = "ui.ruby.configurator.ruby.interpreter.path";
  @NonNls String UI_RBENV_VERSION_NAME_KEY = "ui.ruby.configurator.rbenv.version.name";
  @NonNls String UI_RBENV_FILE_PATH_KEY = "ui.ruby.configurator.rbenv.file.path";

  @NonNls String UI_INNER_RVM_EXIST_REQUIREMENT_KEY = "ui.ruby.configurator.rvm.path";
  @NonNls String UI_INNER_RBENV_EXIST_REQUIREMENT_KEY = "ui.ruby.configurator.rbenv.root.path";
}