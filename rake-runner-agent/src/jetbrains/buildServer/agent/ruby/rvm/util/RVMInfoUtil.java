

package jetbrains.buildServer.agent.ruby.rvm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.rakerunner.scripting.ScriptingRunnersProvider;
import jetbrains.buildServer.agent.rakerunner.scripting.ShellScriptRunner;
import jetbrains.buildServer.agent.ruby.rvm.RVMInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vladislav.Rassokhin
 */
public class RVMInfoUtil {

  public static final Pattern INFO_LINE_PATTERN = Pattern.compile("^[ \\t]*(\\w+):[ \\t]*\"(.*)\"[ \\t]*$");

  @NotNull
  public static RVMInfo gatherInfoUnderRvmShell(@NotNull final String directoryWithRvmrcFile,
                                                @Nullable final Map<String, String> envVariables) {
    final ShellScriptRunner shellScriptRunner = ScriptingRunnersProvider.getRVMDefault().getShellScriptRunner();
    RVMInfo info = new RVMInfo(shellScriptRunner.run("rvm current", directoryWithRvmrcFile, envVariables).getStdout().trim());
    for (RVMInfo.Section section : RVMInfo.Section.values()) {
      final ExecResult output = shellScriptRunner.run("rvm info " + section.name(), directoryWithRvmrcFile, envVariables);
      final String stdout = output.getStdout();
      Map<String, String> ret = new HashMap<String, String>();
      for (String line : stdout.split("\n")) {
        final Matcher matcher = INFO_LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
          continue;
        }
        ret.put(matcher.group(1), matcher.group(2));
      }
      info.setSection(section, ret);
    }
    return info;
  }

}