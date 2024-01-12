

package jetbrains.buildServer.rakerunner;

/**
 * @author Roman.Chernyatchik
 */

public interface RakeRunnerBundle {
  String DEFAULT_RVM_SDK = "system";

  String RUNNER_DESCRIPTION = "Runner for executing Rake tasks, Test::Unit and RSpec tests";
  String RUNNER_DISPLAY_NAME = "Rake";

  String RUNNER_ERROR_TITLE_PROBLEMS_IN_CONF_ON_AGENT = "Failed to run Rake..";
  String RUNNER_ERROR_TITLE_JRUBY_PROBLEMS_IN_CONF_ON_AGENT = "Failed to run JRuby..";
  String MSG_OS_NOT_SUPPORTED = "OS isn't supported!";
}