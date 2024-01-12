

package jetbrains.buildServer.feature;

import com.intellij.util.PathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class RubyEnvConfiguratorBuildFeature extends BuildFeature {
  public static final String NOT_SPECIFIED_GOOD = "<i>not specified</i>";
  public static final String NOT_SPECIFIED_ERR = "<strong>NOT SPECIFIED!</strong>";

  private final String myEditUrl;
  private final Map<String, String> myDefaultParameters;

  public RubyEnvConfiguratorBuildFeature(@NotNull final PluginDescriptor descriptor) {
    myEditUrl = descriptor.getPluginResourcesPath("rubyEnvConfiguratorParams.jsp");

    myDefaultParameters = new HashMap<>(4);
    myDefaultParameters.put(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "unspecified");
    myDefaultParameters.put(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY, ".rvmrc");
    myDefaultParameters.put(RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS, Boolean.TRUE.toString());
    myDefaultParameters.put(RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY, Boolean.TRUE.toString());
  }

  @NotNull
  @Override
  public String getType() {
    return RubyEnvConfiguratorConstants.RUBY_ENV_CONFIGURATOR_FEATURE_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Ruby environment configurator";
  }

  @Override
  public String getEditParametersUrl() {
    return myEditUrl;
  }

  @Override
  public boolean isMultipleFeaturesPerBuildTypeAllowed() {
    return false;
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull final Map<String, String> params) {
    StringBuilder result = new StringBuilder();

    final RubyEnvConfiguratorConfiguration configuration = new RubyEnvConfiguratorConfiguration(params);
    switch (configuration.getType()) {
      case INTERPRETER_PATH: {
        displayParameter(result, "Interpreter path", configuration.getRubySdkPath(), NOT_SPECIFIED_GOOD);
        break;
      }
      case RVM: {
        displayParameter(result, "RVM sdk", configuration.getRVMSdkName(), NOT_SPECIFIED_ERR);
        displayParameter(result, "RVM gemset", configuration.getRVMGemsetName(), NOT_SPECIFIED_GOOD);

        if (configuration.isRVMGemsetCreate()) {
          result.append("Create gemset if does not exist\n");
        }
        break;
      }
      case RVMRC: {
        displayParameter(result, "Path to a '.rvmrc' file", configuration.getRVMRCFilePath(), NOT_SPECIFIED_GOOD);
        break;
      }
      case RVM_RUBY_VERSION: {
        displayParameter(result, "Path to a directory with '.ruby-version' file", configuration.getRVMRubyVersionPath(), "<checkout directory>");
        break;
      }
      case RBENV: {
        displayParameter(result, "rbenv interpreter", configuration.getRbEnvVersion(), NOT_SPECIFIED_ERR);
        break;
      }
      case RBENV_FILE: {
        displayParameter(result, "Path to a directory with '.ruby-version' or '.rbenv-version' file:", configuration.getRbEnvVersionFile(), NOT_SPECIFIED_GOOD);
        break;
      }
    }

    if (configuration.isShouldFailBuildIfNoSdkFound()) {
      result.append("Fail build if Ruby interpreter wasn't found\n");
    }
    return result.toString();
  }

  private static void displayParameter(@NotNull final StringBuilder sb,
                                       @NotNull final String name,
                                       @Nullable final String value,
                                       @NotNull final String emptyValue) {
    sb.append(name).append(": ");
    sb.append(StringUtil.isEmptyOrSpaces(value) ? emptyValue : value);
    sb.append("\n");
  }

  @Override
  public Map<String, String> getDefaultParameters() {
    return myDefaultParameters;
  }

  @Override
  public PropertiesProcessor getParametersProcessor() {
    return new ParametersValidator(myDefaultParameters);
  }

  static class ParametersValidator implements PropertiesProcessor {

    private final Map<String, String> myDefaultParameters;

    public ParametersValidator(@NotNull Map<String, String> defaultParameters) {
      myDefaultParameters = defaultParameters;
    }

    public Collection<InvalidProperty> process(final Map<String, String> properties) {
      final Collection<InvalidProperty> ret = new ArrayList<InvalidProperty>(1);
      if ("unspecified".equalsIgnoreCase(properties.get(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY))) {
        ret.add(new InvalidProperty(RubyEnvConfiguratorConstants.UI_USE_RVM_KEY, "Please select one"));
        return ret;
      }
      final RubyEnvConfiguratorConfiguration configuration = new RubyEnvConfiguratorConfiguration(properties);
      switch (configuration.getType()) {
        case RVM: {
          if (StringUtil.isEmptyOrSpaces(configuration.getRVMSdkName())) {
            ret.add(new InvalidProperty(RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY,
                                        "RVM interpreter name cannot be empty. If you want to use system ruby interpreter please enter 'system'."));
          }
          break;
        }
        case RVMRC: {
          String rvmrcFilePath = StringUtil.emptyIfNull(configuration.getRVMRCFilePath());
          if (!StringUtil.isEmptyOrSpaces(rvmrcFilePath) &&
              !StringUtil.hasParameterReferences(rvmrcFilePath) &&
              !PathUtil.getFileName(rvmrcFilePath).equals(".rvmrc")) {
            ret.add(new InvalidProperty(RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY,
                                        "file name must be '.rvmrc'."));
          }
          break;
        }
        case RBENV: {
          if (StringUtil.isEmptyOrSpaces(configuration.getRbEnvVersion())) {
            ret.add(new InvalidProperty(RubyEnvConfiguratorConstants.UI_RBENV_VERSION_NAME_KEY,
                                        "rbenv interpreter name cannot be empty."));
          }
          break;
        }
      }

      resetExtraProperties(properties, configuration);
      return ret;
    }

    private void resetExtraProperties(@NotNull final Map<String, String> properties,
                                      @NotNull final RubyEnvConfiguratorConfiguration configuration) {
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.INTERPRETER_PATH)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RVM)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY);
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY);
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RVMRC)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RVM_RUBY_VERSION)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RVM_RUBY_VERSION_PATH_KEY);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RVM) &&
        !configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RVMRC) &&
        !configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RVM_RUBY_VERSION)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_INNER_RVM_EXIST_REQUIREMENT_KEY);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RBENV)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RBENV_VERSION_NAME_KEY);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RBENV_FILE)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_RBENV_FILE_PATH_KEY);
      }
      if (!configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RBENV) &&
        !configuration.getType().equals(RubyEnvConfiguratorConfiguration.Type.RBENV_FILE)) {
        resetProperty(properties, RubyEnvConfiguratorConstants.UI_INNER_RBENV_EXIST_REQUIREMENT_KEY);
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