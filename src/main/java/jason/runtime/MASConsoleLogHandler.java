package jason.runtime;


import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/** Logger handler (redirect output to MASConsoleGUI) */
public class MASConsoleLogHandler extends StreamHandler  {

    public static String formaterField = MASConsoleLogHandler.class.getName()+".formatter";
    public static String levelField = MASConsoleLogHandler.class.getName()+".level";
    public static String useColorsPropField = MASConsoleLogHandler.class.getName() + ".colors";

    private MASConsoleGUI fGUI;

    public MASConsoleLogHandler() {
        String colors = LogManager.getLogManager().getProperty(useColorsPropField);
        if (colors != null && colors.equals("true")) {
            fGUI = MASConsoleColorGUI.get();
        } else {
            fGUI = MASConsoleGUI.get();
        }

        //setFormatter(new MASConsoleLogFormatter());
        String formatter = LogManager.getLogManager().getProperty(formaterField);
        if (formatter != null) {
            try {
                setFormatter((Formatter) Class.forName(formatter).newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String level = LogManager.getLogManager().getProperty(levelField);
        if (level != null) {
            try {
                setLevel(Level.parse(level));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void publish(LogRecord l) {
        fGUI.append(MASConsoleLogFormatter.getAgName(l), getFormatter().format(l));
    }
}
