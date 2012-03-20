/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.slow.plugins.rakerunner;

import org.testng.annotations.Test;

/**
 * @author Roman Chernyatchik
 */
@Test(groups = {"all", "slow"})
public class TestSpecMessagesTest extends AbstractTestSpecTest {
  public void testLocation() throws Throwable {
    //TODO implement test location for test-spec!
    setMessagesTranslationEnabled(false);
    setPartialMessagesChecker();

    setMockingOptions();
    initAndDoTest("stat:general", "_location", false);
  }
}