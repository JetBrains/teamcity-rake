

package jetbrains.buildServer.agent.ruby.rbenv.detector;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class RbEnvDetectorFactory {

  @NotNull
  public RbEnvDetector createDetector() {
    return SystemInfo.isUnix ? new RbEnvDetectorForUNIX() : new RbEnvDetectorForUnsupportedOS();
  }

}