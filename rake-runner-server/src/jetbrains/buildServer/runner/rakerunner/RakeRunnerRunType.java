

package jetbrains.buildServer.runner.rakerunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerUtils;
import jetbrains.buildServer.runner.BuildFileRunnerConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman Chernyatchik
 */
public class RakeRunnerRunType extends RunType {

  private final PluginDescriptor myDescriptor;
  private final Map<String, String> myDefaultParameters;

  public RakeRunnerRunType(@NotNull final PluginDescriptor descriptor, @NotNull final RunTypeRegistry runTypeRegistry) {
    myDescriptor = descriptor;
    runTypeRegistry.registerRunType(this);

    myDefaultParameters = new HashMap<String, String>();
    final String trueStr = Boolean.TRUE.toString();
    // by default let's enable : Test::Unit, RSpec, Cucumber
    myDefaultParameters.put(RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY, trueStr);
    myDefaultParameters.put(RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY, trueStr);
    myDefaultParameters.put(RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY, trueStr);
    // configuration version
    myDefaultParameters.put(RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY,
            RakeRunnerConstants.CURRENT_CONFIG_VERSION);
    // select 'default/rec' ruby interpreter mode by default:
    RakeRunnerUtils.setConfigMode(RakeRunnerUtils.RubyConfigMode.DEFAULT, myDefaultParameters);
    // select 'bundle exec' by default
    myDefaultParameters.put(RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY, trueStr);
  }

  @Override
  @Nullable
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new ParametersValidator(myDefaultParameters);
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return "taskRunnerRunParams.jsp";
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return "viewTaskRunnerRunParams.jsp";
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return myDefaultParameters;
  }

  @NotNull
  @Override
  public String getDescription() {
    return RakeRunnerBundle.RUNNER_DESCRIPTION;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return RakeRunnerBundle.RUNNER_DISPLAY_NAME;
  }

  @NotNull
  @Override
  public String getType() {
    return RakeRunnerConstants.RUNNER_TYPE;
  }

  @Nullable
  @Override
  public String getIconUrl() {
    return myDescriptor.getPluginResourcesPath("rake-runner.svg");
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull final Map<String, String> parameters) {
    StringBuilder result = new StringBuilder();
    if (parameters.get(BuildFileRunnerConstants.USE_CUSTOM_BUILD_FILE_KEY) != null) {
      result.append("Rake file: custom");
    } else {
      result.append("Rake file path: ").append(StringUtil.emptyIfNull(parameters.get(BuildFileRunnerConstants.BUILD_FILE_PATH_KEY)));
    }
    result.append("\n");

    final String tasks = parameters.get(RakeRunnerConstants.SERVER_UI_RAKE_TASKS_PROPERTY);
    result.append("Rake tasks: ").append(StringUtil.isEmpty(tasks) ? "default" : tasks);
    result.append("\n");

    result.append("Ruby interpreter: ");
    switch (RakeRunnerUtils.getRubyInterpreterConfigMode(parameters)) {
      case DEFAULT:
        result.append("Default / REC feature");
        break;
      case INTERPRETER_PATH:
        final String rubyInterpreterPath = RakeRunnerUtils.getRubySdkPath(parameters);
        result.append("Path: ").append(rubyInterpreterPath);
        break;
      case RVM:
        final String rvmSdkName = RakeRunnerUtils.getRVMSdkName(parameters);

        result.append("RVM interpreter: ").append(rvmSdkName != null ? rvmSdkName
                                                                     : RakeRunnerBundle.DEFAULT_RVM_SDK);
        final String gemset = RakeRunnerUtils.getRVMGemsetName(parameters);
        if (gemset != null) {
          result.append('@').append(gemset);
        }
        break;
    }
    return result.toString();
  }

  static class ParametersValidator implements PropertiesProcessor {

    private final Map<String, String> myDefaultParameters;

    public ParametersValidator(@NotNull Map<String, String> defaultParameters) {
      myDefaultParameters = defaultParameters;
    }

    public Collection<InvalidProperty> process(final Map<String, String> properties) {
      final Collection<InvalidProperty> ret = new ArrayList<InvalidProperty>(1);
      final RakeRunnerUtils.RubyConfigMode mode = RakeRunnerUtils.getRubyInterpreterConfigMode(properties);
      switch (mode) {
        //case INTERPRETER_PATH: {
        //  if (StringUtil.isEmpty(RakeRunnerUtils.getRubySdkPath(properties))) {
        //    ret.add(new InvalidProperty(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH, "Interpeter path must be specified."));
        //  }
        //  break;
        //}
        case RVM: {
          if (StringUtil.isEmpty(RakeRunnerUtils.getRVMSdkName(properties))) {
            ret.add(new InvalidProperty(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME, "The interpeter name must be specified."));
          }
          break;
        }
      }
      resetExtraProperties(properties);
      return ret;
    }

    private void resetExtraProperties(@NotNull final Map<String, String> properties) {
      final RakeRunnerUtils.RubyConfigMode mode = RakeRunnerUtils.getRubyInterpreterConfigMode(properties);
      if (!mode.equals(RakeRunnerUtils.RubyConfigMode.INTERPRETER_PATH)) {
        resetProperty(properties, RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH);
      }
      if (!mode.equals(RakeRunnerUtils.RubyConfigMode.RVM)) {
        resetProperty(properties, RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME);
        resetProperty(properties, RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME);
      }

      if ("true".equals(properties.get(BuildFileRunnerConstants.USE_CUSTOM_BUILD_FILE_KEY))) {
        resetProperty(properties, BuildFileRunnerConstants.BUILD_FILE_PATH_KEY);
      } else {
        resetProperty(properties, BuildFileRunnerConstants.BUILD_FILE_KEY);
      }
    }

    private void resetProperty(@NotNull final Map<String, String> properties, @NotNull final String key) {
      if (myDefaultParameters.containsKey(key)) {
        properties.put(key, myDefaultParameters.get(key));
      } else {
        properties.remove(key);
      }
    }
  }
}