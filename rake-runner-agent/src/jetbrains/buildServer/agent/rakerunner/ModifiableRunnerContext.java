package jetbrains.buildServer.agent.rakerunner;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.rakerunner.utils.EnvironmentPatchableMap;
import jetbrains.buildServer.agent.rakerunner.utils.FileUtil2;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladislav.Rassokhin
 */
public class ModifiableRunnerContext {
  @NotNull private final Map<String, String> myRunnerParameters;
  @NotNull private final Map<String, String> myBuildParameters;
  @NotNull private final EnvironmentPatchableMap myEnvParameters;
  @NotNull private final String myCheckoutDirectory;
  @NotNull private final String myWorkingDirectory;

  public ModifiableRunnerContext(@NotNull final BuildRunnerContext origin) throws RunBuildException {
    myRunnerParameters = new HashMap<String, String>(origin.getRunnerParameters());
    myBuildParameters = new HashMap<String, String>(origin.getBuildParameters().getAllParameters());
    myEnvParameters = new EnvironmentPatchableMap(origin.getBuildParameters().getEnvironmentVariables());
    myCheckoutDirectory = FileUtil2.getCanonicalPath(origin.getBuild().getCheckoutDirectory());
    myWorkingDirectory = FileUtil2.getCanonicalPath(origin.getWorkingDirectory());
  }

  @NotNull
  public Map<String, String> getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  public Map<String, String> getBuildParameters() {
    return myBuildParameters;
  }

  @NotNull
  public EnvironmentPatchableMap getEnvParameters() {
    return myEnvParameters;
  }

  @NotNull
  public String getCheckoutDirectory() {
    return myCheckoutDirectory;
  }

  @NotNull
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }
}
