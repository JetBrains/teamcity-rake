

package jetbrains.buildServer.rakerunner;

import com.intellij.openapi.util.text.StringUtil;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public class RakeRunnerUtils {
  public enum RubyConfigMode {
    DEFAULT("default"),
    INTERPRETER_PATH("path"),
    RVM("rvm");

    private final String myModeString;

    RubyConfigMode(@NotNull final String modeString) {
      myModeString = modeString;
    }

    public boolean isThisMode(@Nullable final String modeStrValue) {
      return myModeString.equals(modeStrValue);
    }

    @NotNull
    public String getModeValueString() {
      return myModeString;
    }
  }

  @NotNull
  public static RubyConfigMode getRubyInterpreterConfigMode(@NotNull final Map<String, String> runParams) {
    final String modeStrValue = runParams.get(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE);
    if (modeStrValue != null) {
      for (RubyConfigMode mode : RubyConfigMode.values()) {
        if (mode.isThisMode(modeStrValue)) {
          return mode;
        }
      }
    }
    return RubyConfigMode.DEFAULT;
  }

  public static void setConfigMode(@NotNull final RubyConfigMode mode,
                                   @NotNull final Map<String, String> runParams) {
    runParams.put(RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE, mode.getModeValueString());
  }

  @Nullable
  public static String getRubySdkPath(@NotNull final Map<String, String> params) {
    final String value = params.get(RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMGemsetName(@NotNull final Map<String, String> params) {
    final String value = params.get(RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME);
    return StringUtil.isEmpty(value) ? null : value;
  }

  @Nullable
  public static String getRVMSdkName(@NotNull final Map<String, String> params) {
    final String value = params.get(RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME);
    return StringUtil.isEmpty(value) ? null : value;
  }
}