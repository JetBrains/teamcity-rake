

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.rbenv.InstalledRbEnv;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RbEnvDetectorForUnsupportedOS extends RbEnvDetector {

  @Nullable
  @Override
  public InstalledRbEnv detect(@NotNull final Map<String, String> environmentParams) {
    return null;
  }
}