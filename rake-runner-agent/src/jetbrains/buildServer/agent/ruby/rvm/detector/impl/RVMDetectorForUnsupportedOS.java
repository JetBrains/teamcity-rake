

package jetbrains.buildServer.agent.ruby.rvm.detector.impl;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.agent.ruby.rvm.detector.RVMDetector;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMDetectorForUnsupportedOS extends RVMDetector {

  @Override
  public InstalledRVM detect(@NotNull final Map<String, String> env) {
    return null;
  }
}