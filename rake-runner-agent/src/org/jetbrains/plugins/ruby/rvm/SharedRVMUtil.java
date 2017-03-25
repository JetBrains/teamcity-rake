/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.ruby.rvm.InstalledRVM;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.VersionComparatorUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 *         <p/>
 *         Shared with TeamCity
 */
public class SharedRVMUtil {
  public interface Constants {
    String LOCAL_RVM_HOME_FOLDER_NAME = ".rvm";
    String RVM_GEMS_FOLDER_NAME = "gems";
    String RVM_RUBIES_FOLDER_NAME = "rubies";
    String RVM_BIN_FOLDER_RELATIVE_PATH = "/bin";
    String DEFAULT_GEMSET_SEPARATOR = "@";
    String RVM_GEMSET_SEPARATOR_ENVVAR = "rvm_gemset_separator";
    String GLOBAL_GEMSET_NAME = "global";
    String GEM_PATH = "GEM_PATH";
    String GEM_HOME = "GEM_HOME";
    String BUNDLE_PATH = "BUNDLE_PATH";
    String MY_RUBY_HOME = "MY_RUBY_HOME";
    String IRBRC = "IRBRC";
    String GEMS_ROOT_RELATIVE_PATH = "/gems";
    String RVM_RUBY_STRING = "rvm_ruby_string";
    String RVM_GEMSET = "gemset";

    String[] SYSTEM_RVM_ENVVARS_TO_RESET = new String[]{
      SharedRVMUtil.Constants.GEM_HOME,
      SharedRVMUtil.Constants.GEM_PATH,
      SharedRVMUtil.Constants.BUNDLE_PATH,
      SharedRVMUtil.Constants.MY_RUBY_HOME,
      SharedRVMUtil.Constants.RVM_GEMSET,
      SharedRVMUtil.Constants.IRBRC
    };
  }

  private SharedRVMUtil() {
  }

  @NotNull
  public static Pair<String, String> determineSuitableRVMSdkDist(@NotNull final String rvmrcSdkRef,
                                                                 @Nullable final String rvmrcGemset,
                                                                 @NotNull final RubyDistToGemsetTable distName2GemsetsTable) {
    final InstalledRVM rvm = RVMPathsSettings.getRVMNullSafe();
    final String resolved = rvm.getDistrForName(rvmrcSdkRef);
    if (resolved == null) {
      return Pair.create(null,null);
    }
    final Set<String> installed = rvm.getInstalledRubies();
    if (installed.contains(resolved)) {
      // check gemsets
      for (String gemset : distName2GemsetsTable.getGemsets(resolved)) {
        if (areGemsetsEqual(rvmrcGemset, gemset)) {
          return Pair.create(resolved, gemset);
        }
      }
      return Pair.create(resolved, null);
    } else {
      // RVM cannot resolve such name into interpreter name
      // May be caused by RVM update (when installed 'ruby-1.8.7-pX' and requested '1.8.7' resolved into 'ruby-1.8.7-pY' by RVM)
      // In such case we can try to resolve manually, but should TODO: notify user.

      // Reverse ordered installed interpreters (from higher version to old)
      final SortedSet<String> ordered = new TreeSet<String>(installed).descendingSet();
      final List<String> possible = CollectionsUtil.filterCollection(ordered, new Filter<String>() {
        public boolean accept(@NotNull final String data) {
          // check either refName suites dist name or not
          return sdkRefMatchesManual(rvmrcSdkRef, data);
        }
      });
      // First try more suitable interpreter
      for (String dist : possible) {
        // check gemsets
        for (String gemset : distName2GemsetsTable.getGemsets(dist)) {
          if (areGemsetsEqual(rvmrcGemset, gemset)) {
            return Pair.create(dist, gemset);
          }
        }
        return Pair.create(dist, null);
      }
    }
    return Pair.create(null,null);
  }

  public static boolean sdkRefMatchesManual(@NotNull final String sdkRef,
                                            @NotNull final String distName) {
    // starts with
    // e.g. [ruby-1.8.7] and [ruby-1.8.7-p249, ruby-1.8.7, ruby-1.8.7-2010.01]
    // TODO [romeo]: rvm use jruby will chose jruby with highest version!
    if (distName.startsWith(sdkRef)) {
      return true;
    }

    // Starts with number
    if (Character.digit(sdkRef.charAt(0), 10) != -1) {
      if (VersionComparatorUtil.compare(sdkRef, "1.8") >= 0) {
        // Looks like ruby
        if (distName.startsWith("ruby-" + sdkRef)) {
          return true;
        }
      } else {
        // Looks like jruby
        if (distName.startsWith("jruby-" + sdkRef)) {
          return true;
        }
      }
    }

    // Ignore patchversion (match ruby-2.1.0-p0 to ruby-2.1.0)
    final Matcher matcher = Pattern.compile("(.*)\\-p([0-9]+)").matcher(sdkRef);
    if (matcher.matches()) {
      // sdkRef have patchversion
      final String ref = matcher.group(1);
      return sdkRefMatchesManual(ref, distName);
    }

    return false;
  }

  public static String getGemsetSeparator() {
    if (SystemInfo.isUnix) {
      // RVM supported on Unix systems

      // 1. get from env variable
      // (e.g. Linux or good Mac)
      final String separator = System.getenv(Constants.RVM_GEMSET_SEPARATOR_ENVVAR);
      if (separator != null) {
        return separator;
      }
    }
    return Constants.DEFAULT_GEMSET_SEPARATOR;
  }

  public static void registerGemset(@NotNull final String rvmGemsSubFolder,
                                    @NotNull final Condition<String> isRVMDistCondition,
                                    @NotNull final RubyDistToGemsetTable rubyDist2Gemset) {
    final int separatorIndex = rvmGemsSubFolder.indexOf(getGemsetSeparator());

    // 1. [dist][@][gemset] or just [dist] with default gemset
    final String distName = separatorIndex == -1 ? rvmGemsSubFolder : rvmGemsSubFolder.substring(0, separatorIndex);
    final String gemset = separatorIndex == -1 ? null : rvmGemsSubFolder.substring(separatorIndex + 1);

    // 2. after invoking rvm command with illegal arguments rvm creates tons of garbage
    // for such not existing sdks and gemsets
    // e.g. "ruby-1.9.1@projecta/" instead of "/ruby-1.9.1-p378@projecta/"
    // such rake ruby sdks will not be registered in ~/.rvm/rubies
    //
    // so let's do initial spam-check:
    if (!isRVMDistCondition.value(distName)) {
      // garbage detected!
      return;
    }

    // 3. add gem to table. We won't ignore "global" gempath just to allow user install gems in
    // "shared" gem path from RubyMine
    rubyDist2Gemset.putGemset(StringUtil.isEmpty(gemset) ? null : gemset, distName);
  }

  public static boolean areGemsetsEqual(@Nullable final String gemset1,
                                        @Nullable final String gemset2) {
    // if gemset is default (null) or custom
    return StringUtil.areEqual(gemset1, gemset2);
  }

  public static boolean canOverride(@NotNull final String envVariable,
                                    @NotNull final Map<String, String> userDefinedEnvVars,
                                    @Nullable final Map<String, String> defaultEnv) {
    if (!userDefinedEnvVars.containsKey(envVariable)) {
      return true;
    }

    if (defaultEnv != null) {
      // if not null than allowed to be overridden

      // check whether useDefinedEnvs contains default value or not.
      final String userValue = userDefinedEnvVars.get(envVariable);
      final String defaultValue = defaultEnv.get(envVariable);
      if (userValue == null) {
        // both need to be null
        return defaultValue == null;
      } else {
        // both need to be equal
        return userValue.equals(defaultValue);
      }
    }
    return false;
  }

  public static class RubyDistToGemsetTable {
    private final Map<String, List<String>> myRubyDist2Gemset;

    public RubyDistToGemsetTable() {
      this(new HashMap<String, List<String>>());
    }

    public static RubyDistToGemsetTable emptyTable() {
      return new RubyDistToGemsetTable(Collections.<String, List<String>>emptyMap());
    }

    private RubyDistToGemsetTable(Map<String, List<String>> tableImpl) {
      myRubyDist2Gemset = new HashMap<String, List<String>>();
    }

    public void putGemset(final String gemsetName,
                          final String distName) {
      List<String> gemsets = myRubyDist2Gemset.get(distName);
      if (gemsets == null) {
        gemsets = new ArrayList<String>(1);
        myRubyDist2Gemset.put(distName, gemsets);
      }
      // put gemset
      gemsets.add(gemsetName);
    }

    public boolean isEmpty() {
      return myRubyDist2Gemset.isEmpty();
    }

    public List<String> getGemsets(final String distName) {
      return myRubyDist2Gemset.get(distName);
    }

    public Set<String> getDists() {
      return myRubyDist2Gemset.keySet();
    }
  }
}