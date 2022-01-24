/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.rakerunner.utils;

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.List;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RubySDKUtilTest extends BaseTestCase {
  @Test
  public void testFindGemsByNameDoesNotReturnLongerNamesWithPrefix() throws Exception {
    File tempDir = createTempDir();
    File gems = createGems(tempDir, "bundler-1.10.0", "bundler-1.12.5", "bundler-unload-1.0.2");
    String gemsBasePath = gems.getAbsolutePath() + File.separator;
    List<Pair<String, String>> gemsByName = RubySDKUtil.findGemsByName("bundler", new String[]{tempDir.getAbsolutePath()});
    List<Pair<String, String>> converted = CollectionsUtil.convertCollection(gemsByName, new Converter<Pair<String, String>, Pair<String, String>>() {
      @Override
      public Pair<String, String> createFrom(@NotNull final Pair<String, String> source) {
        return Pair.create(StringUtil.removeSuffix(source.first.replace(gemsBasePath, ""), "-" + source.second, true), source.second);
      }
    });

    //noinspection unchecked
    then(converted).containsOnly(Pair.create("bundler", "1.10.0"), Pair.create("bundler", "1.12.5"));
  }

  private File createGems(final File base, final String... names) throws IOException {
    File gemsDir = new File(base, "gems");
    for (String name : names) {
      FileUtil.createDir(new File(gemsDir, name));
    }
    return gemsDir;
  }
}