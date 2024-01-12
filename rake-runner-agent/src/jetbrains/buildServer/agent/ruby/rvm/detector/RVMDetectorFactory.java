

package jetbrains.buildServer.agent.ruby.rvm.detector;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.agent.ruby.rvm.detector.impl.RVMDetectorForUNIX;
import jetbrains.buildServer.agent.ruby.rvm.detector.impl.RVMDetectorForUnsupportedOS;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMDetectorFactory {

  @NotNull
  public RVMDetector createRVMDetector() {
    return SystemInfo.isUnix ? new RVMDetectorForUNIX() : new RVMDetectorForUnsupportedOS();
  }

}