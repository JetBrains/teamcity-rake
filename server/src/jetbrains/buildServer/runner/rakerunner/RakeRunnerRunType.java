/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package jetbrains.buildServer.runner.rakerunner;

import jetbrains.buildServer.util.PropertiesUtil;
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
            public Collection<InvalidProperty> process(Map properties) {
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
        //TODO - setup check box options
        return map;
    }

    public boolean isCheckoutTypeSupported(final SBuildType.CheckoutType checkoutType) {
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