//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

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
