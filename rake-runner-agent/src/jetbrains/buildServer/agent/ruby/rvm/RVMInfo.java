

package jetbrains.buildServer.agent.ruby.rvm;

import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * "rvm info" for one interpreter
 *
 * @author Vladislav.Rassokhin
 */
public class RVMInfo {

  private final String myInterpreterName;
  private final Map<Section, Map<String, String>> mySectionsValues = new EnumMap<Section, Map<String, String>>(Section.class);

  public RVMInfo(@NotNull final String interpreterNameWithGemset) {
    final String trimmed = interpreterNameWithGemset.trim();
    final int i = trimmed.indexOf("@");
    if (i != -1) {
      myInterpreterName = trimmed.substring(0, i);
    } else {
      myInterpreterName = trimmed;
    }
  }

  public enum Section {
    system,
    rvm,
    ruby,
    homes,
    binaries,
    environment
  }

  @NotNull
  public String getInterpreterName() {
    return myInterpreterName;
  }

  @NotNull
  public Map<String, String> getSection(@NotNull final Section section) {
    return mySectionsValues.get(section);
  }

  public void setSection(@NotNull final Section section, @NotNull final Map<String, String> values) {
    mySectionsValues.put(section, values);
  }
}