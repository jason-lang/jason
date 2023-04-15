package jason.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

/**
 * Default formatter for Jason output.
 */
public class MASConsoleLogFormatter extends java.util.logging.Formatter {

    public String format(LogRecord l) {
        StringBuilder s = new StringBuilder("[");
        s.append(getAgName(l));
        s.append("] ");
        s.append(l.getMessage());
        if (l.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            l.getThrown().printStackTrace(pw);
            s.append('\n');
            s.append(sw);
        }
        s.append('\n');
        return s.toString();
    }

    public static String getAgName(LogRecord l) {
        String lname = l.getLoggerName();
        int posd = lname.lastIndexOf('.');
        if (posd > 0) {
            return lname.substring(posd+1);
        }
        return lname;
    }
}
