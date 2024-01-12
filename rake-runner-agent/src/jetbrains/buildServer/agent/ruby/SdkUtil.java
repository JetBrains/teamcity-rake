

package jetbrains.buildServer.agent.ruby;

import jetbrains.buildServer.agent.ruby.rbenv.RbEnvRubySdk;
import jetbrains.buildServer.agent.ruby.rvm.RVMRubySdk;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class SdkUtil {

  public static boolean isRvmSdk(@NotNull final RubySdk sdk) {
    return sdk instanceof RVMRubySdk;
  }
  public static boolean isRbEnvSdk(@NotNull final RubySdk sdk) {
    return sdk instanceof RbEnvRubySdk;
  }
}