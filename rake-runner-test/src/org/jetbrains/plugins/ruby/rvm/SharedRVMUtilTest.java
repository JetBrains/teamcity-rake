/*
 * Copyright 2000-2019 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.rvm;

import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Vladislav.Rassokhin
 */
@TestFor(testForClass = SharedRVMUtil.class)
@Test(groups = "unix")
public class SharedRVMUtilTest {
  @Test(dataProvider = "data")
  public void testSdkRefMatchesManual(@NotNull final String from,
                                      @NotNull final String full) throws Exception {
    Assert.assertTrue(SharedRVMUtil.sdkRefMatchesManual(from, full));
  }

  @DataProvider
  public static String[][] data() {
    return new String[][]{
      {"ruby", "ruby-1.8.7-p37"},
      {"ruby", "ruby-1.9.2-p320"},
      {"1.8.7", "ruby-1.8.7-p37"},
      {"1.8.7", "ruby-1.8.7-p374"},
      {"ruby-1.8.7", "ruby-1.8.7-p374"},
      {"1.8.7-p374", "ruby-1.8.7-p374"},
      {"ruby-1.8.7-p374", "ruby-1.8.7-p374"},
      {"1.9.2", "ruby-1.9.2-p320"},
      {"ruby-1.9.2", "ruby-1.9.2-p320"},
      {"1.9.3", "ruby-1.9.3-p448"},
      {"ruby-1.9.3", "ruby-1.9.3-p448"},
      {"jruby", "jruby-1.7.4"},
      {"jruby-1.7.4", "jruby-1.7.4"},
      {"jruby-head", "jruby-head"},

      {"jruby", "jruby-1.7.4"},
      {"1.6", "jruby-1.6.0"},
      {"1.5", "jruby-1.5.0"},
      {"1.4", "jruby-1.4.0"},
      {"1.3", "jruby-1.3.0"},

      {"ruby", "ruby-1.9.3"},
      {"1.8", "ruby-1.8.0"},
      {"1.9", "ruby-1.9.0"},
      {"2.0", "ruby-2.0.0"},

      {"rbx", "rbx-head"},
      {"rbx-head", "rbx-head"},
      {"ree", "ree-1.8.7-2012.02"},
      {"ree-1.8.7", "ree-1.8.7-2012.02"},
      {"ree-1.8.7-2012.02", "ree-1.8.7-2012.02"},
      {"macruby", "macruby-0.12"},
      {"macruby-0.12", "macruby-0.12"},
      {"maglev", "maglev-head"},
      {"maglev-head", "maglev-head"},
      {"ironruby", "ironruby-1.1.3"},
      {"ironruby-1.1.3", "ironruby-1.1.3"},

      {"ruby-2.1.0-p0", "ruby-2.1.0-p0"},
      {"ruby-2.1.0-p0", "ruby-2.1.0"},
      {"2.1.0-p0", "ruby-2.1.0"},
      {"2.1.0", "ruby-2.1.0"},
      {"ruby-2.1.0", "ruby-2.1.0-p0"},
      //{"", ""},
    };
  }

}
