

package jetbrains.buildServer.agent.ruby;

import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Anna Bulenkova
 * @author Vladislav.Rassokhin
 */
public abstract class RubyVersionManager {
  private final String myName;

  protected RubyVersionManager(@NotNull final String name) {
    myName = name;
  }

  @Nullable
  public abstract File getHome();

  @Nullable
  public abstract File getRubiesFolder();

  public abstract boolean isSupportedByOs();

  @NotNull
  public String getName() {
    return myName;
  }
}