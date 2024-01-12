

package jetbrains.slow.plugins.rakerunner;

/**
 * @author Roman Chernyatchik
 */
public enum MockingOptions {
  FAKE_STACK_TRACE(":fake_stacktrace"),
  FAKE_ERROR_MSG(":fake_error_msg"),
  FAKE_LOCATION_URL(":fake_location_url"),
  FAKE_TIME(":fake_time");

  ////////////
  private static final String TEAMCITY_RAKERUNNER_DEBUG_OPTIONS_ENV = "TEAMCITY_RAKERUNNER_DEBUG_OPTIONS";

  private final String myOptionName;

  MockingOptions(final String optionName) {
    myOptionName = optionName;
  }

  public String getOptionName() {
    return myOptionName;
  }

  public static String getEnvVarName() {
    return TEAMCITY_RAKERUNNER_DEBUG_OPTIONS_ENV;
  }

  public static String getEnvVarValue(final MockingOptions[] options) {
    final StringBuilder buff = new StringBuilder();
    for (MockingOptions option : options) {
      buff.append(option.getOptionName()).append(' ');
    }
    return buff.toString();
  }
}