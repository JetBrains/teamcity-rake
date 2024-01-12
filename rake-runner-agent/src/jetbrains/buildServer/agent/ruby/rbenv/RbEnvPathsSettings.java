

package jetbrains.buildServer.agent.ruby.rbenv;

import java.util.Map;
import jetbrains.buildServer.agent.ruby.rbenv.detector.RbEnvDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RbEnvPathsSettings {
  private static RbEnvPathsSettings ourInstance;

  @NotNull
  private final RbEnvDetector myDetector;

  @Nullable
  private InstalledRbEnv myInstalledRbEnv;

  public RbEnvPathsSettings(@NotNull final RbEnvDetector detector) {
    myDetector = detector;
    ourInstance = this;
  }

  public static RbEnvPathsSettings getInstance() {
    return ourInstance;
  }


  public void initialize(@NotNull final Map<String, String> env) {
    myInstalledRbEnv = myDetector.detect(env);
  }

  @Nullable
  public InstalledRbEnv getRbEnv() {
    return myInstalledRbEnv;
  }

  /**
   * Null-safe version of (getInstance().getRbEnv()) for getting current known rbenv.
   * Be sure that rbenv known at execution time.
   *
   * @return known rvm
   * @throws IllegalStateException if rvm is null
   */
  @NotNull
  public static InstalledRbEnv getRbEnvNullSafe() {
    final InstalledRbEnv rvm = getInstance().getRbEnv();
    if (rvm == null) {
      throw new IllegalStateException("Unexpected: rbenv is null. Cannot be null at that step.");
    }
    return rvm;
  }
}