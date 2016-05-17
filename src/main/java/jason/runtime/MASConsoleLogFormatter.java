//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini and Jomi F. Hubner
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
