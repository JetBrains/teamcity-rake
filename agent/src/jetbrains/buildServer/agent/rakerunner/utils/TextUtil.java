package jetbrains.buildServer.agent.rakerunner.utils;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman.Chernyatchik
 * @date: 24.12.2007
 */
public class TextUtil {
    public static String removeNewLine(String s) {
        if (s.length() == 0) return s;
        if (s.charAt(s.length() - 1) == '\n')
            s = s.substring(0, s.length() - 1);
        if (s.charAt(s.length() - 1) == '\r')
            s = s.substring(0, s.length() - 1);
        return s;
    }
}
