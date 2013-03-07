package jetbrains.slow.plugins.rakerunner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Vladislav.Rassokhin
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestWithGemfiles {
  public String[] value();
}
