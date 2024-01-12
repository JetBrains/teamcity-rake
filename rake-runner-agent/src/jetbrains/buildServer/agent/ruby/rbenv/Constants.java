

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