

package jetbrains.slow.plugins.rakerunner;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author Vladislav.Rassokhin
 */
public class BundlerBasedTestsDataProvider {

  public static final Set<String> CUCUMBER_GEMFILES_CONFIGURATIONS = ImmutableSet.of("cucumber-trunk");
  public static final Set<String> CUCUMBER4_GEMFILES_CONFIGURATIONS = ImmutableSet.of("cucumber-4-trunk");
  public static final Set<String> TEST_UNIT_GEMFILES_CONFIGURATIONS = ImmutableSet.of("test-unit-trunk");
  public static final Set<String> TEST_SPEC_GEMFILES_CONFIGURATIONS = ImmutableSet.of("test-spec-trunk");
  public static final Set<String> RSPEC_GEMFILES_CONFIGURATIONS = ImmutableSet.of("rspec-trunk", "rspec-3");
  public static final Set<String> SHOULDA_GEMFILES_CONFIGURATIONS = ImmutableSet.of("shoulda-trunk");

  @NotNull
  @DataProvider(name = "cucumber")
  public static Iterator<Object[]> getCucumberDP() {
    return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
        CUCUMBER_GEMFILES_CONFIGURATIONS);
  }

  @NotNull
  @DataProvider(name = "cucumber4")
  public static Iterator<Object[]> getCucumber4DP() {
    return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
                                                                CUCUMBER4_GEMFILES_CONFIGURATIONS);
  }


  @NotNull
  @DataProvider(name = "test-unit")
  public static Iterator<Object[]> getTestUnitDP() {
    return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
        TEST_UNIT_GEMFILES_CONFIGURATIONS);
  }

  @NotNull
  @DataProvider(name = "test-spec")
  public static Iterator<Object[]> getTestSpecDP() {
    return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
        TEST_SPEC_GEMFILES_CONFIGURATIONS);
  }

  @NotNull
  @DataProvider(name = "shoulda")
  public static Iterator<Object[]> getShouldaDP() {
    return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
        SHOULDA_GEMFILES_CONFIGURATIONS);
  }

  @NotNull
  @DataProvider(name = "rspec")
  public static Iterator<Object[]> getRSpecDP() {
    return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
        RSPEC_GEMFILES_CONFIGURATIONS);
  }

  // DO NOT USE! TODO: Waiting for next TestNG release (>6.8) for Constructor in @DataProvider support.
  @NotNull
  @DataProvider(name = "annotated")
  public static Iterator<Object[]> getAnnotatedDP(@NotNull final Constructor constructor) {
    final TestWithGemfiles annotation = (TestWithGemfiles) constructor.getAnnotation(TestWithGemfiles.class);
    if (annotation == null) {
      return Collections.<Object[]>emptySet().iterator();
    } else {
      return RubyVersionsDataProvider.getCartesianProductIterator(RubyVersionsDataProvider.getRubyVersionsSet(),
          new LinkedHashSet<String>(Arrays.asList(annotation.value())));
    }
  }

}