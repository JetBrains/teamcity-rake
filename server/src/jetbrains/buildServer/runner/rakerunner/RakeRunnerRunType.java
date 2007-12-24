package jetbrains.buildServer.runner.rakerunner;

import jetbrains.buildServer.BuildTypeDescriptor;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.runner.BuildFileRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerConstants;
import jetbrains.buildServer.rakerunner.RakeRunnerBundle;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.web.openapi.WebResourcesManager;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 03.06.2007
 */
public class RakeRunnerRunType extends RunType {
    private static final String RAKE_RUNNER_SERVER_PLUGIN_FILE_NAME = "rakeRunnerServer.jar";

    public RakeRunnerRunType(final RunTypeRegistry runTypeRegistry,
                             final WebResourcesManager resourcesManager) {
        resourcesManager.addPluginResources("rake-runner", RAKE_RUNNER_SERVER_PLUGIN_FILE_NAME);
        runTypeRegistry.registerRunType(this);
    }

    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new PropertiesProcessor() {
            public Collection process(Map properties) {
                final List<InvalidProperty> result = new Vector<InvalidProperty>();

                // Rake task name
                final String rakeTaskName =
                        (String)properties.get(RakeRunnerConstants.SERVER_UI_RAKE_TASK_PROPERTY);
                if (PropertiesUtil.isEmptyOrNull(rakeTaskName)) {
                    result.add(new InvalidProperty(RakeRunnerConstants.SERVER_UI_RAKE_TASK_PROPERTY,
                                                   "Rake task name must be specified"));
                }
                return result;
            }
        };
    }

    public String getEditRunnerParamsJspFilePath() {
        return "taskRunnerRunParams.jsp";
    }

    public String getViewRunnerParamsJspFilePath() {
        return "viewTaskRunnerRunParams.jsp";
    }

    public Map<String, String> getDefaultRunnerProperties() {
        final Map<String, String> map = new HashMap<String, String>();
        //TODO
        return map;
    }

    public boolean isCheckoutTypeSupported(final SBuildType.CheckoutType checkoutType) {
//        return checkoutType != BuildTypeDescriptor.CheckoutType.MANUAL;
        return true;
    }

    public String getDescription() {
        return RakeRunnerBundle.RUNNER_DESCRIPTION;
    }

    public String getDisplayName() {
        return RakeRunnerBundle.RUNNER_DISPLAY_NAME;
    }

    public String getType() {
        return RakeRunnerConstants.RUNNER_TYPE;
    }
}