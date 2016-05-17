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

package jason.jeditplugin;

import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.BufferUpdate;

import jason.mas2j.MAS2JProject;
import jason.util.Config;

public class JasonIDPlugin extends EBPlugin {
    public static final String NAME            = "jason";
    public static final String MENU            = "jason.menu";
    public static final String PROPERTY_PREFIX = "plugin.jason.";
    public static final String OPTION_PREFIX   = "options.jason.";

    static {
        try {
            // set some properties
            org.gjt.sp.jedit.jEdit.setProperty("sidekick.parser.asl.label", "AgentSpeak");
            org.gjt.sp.jedit.jEdit.setProperty("mode.asl.sidekick.parser", "asl");
            org.gjt.sp.jedit.jEdit.setProperty("sidekick.parser.mas2j_parser.label", "JasonProject");
            org.gjt.sp.jedit.jEdit.setProperty("mode.mas2j.sidekick.parser", "mas2j");
            
            Class.forName(JasonIDOptionPanel.class.getName()); // just to run static init
        } catch (Exception e) {
        }
    }

    public void handleMessage(EBMessage msg) {
        if (org.gjt.sp.jedit.jEdit.getViews().length > 0) {
            final DockableWindowManager d = org.gjt.sp.jedit.jEdit.getViews()[0].getDockableWindowManager();
            if (d.getDockableWindow(NAME) == null) {
                /*
                 * if (!d.isDockableWindowVisible(NAME)) {
                 * d.addDockableWindow(NAME); //d.floatDockableWindow(NAME);
                 * //Log.log(Log.DEBUG,this,"Add to dock"); //JasonID jid =
                 * (JasonID)d.getDockableWindow(NAME); //jid.start(); } } else {
                 */
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        d.addDockableWindow(NAME);
                    }
                });
            }
        }

        if (msg != null && msg instanceof BufferUpdate) {
            final BufferUpdate bu = (BufferUpdate) msg;
            if ((bu.getWhat() == BufferUpdate.LOADED || bu.getWhat() == BufferUpdate.CREATED)) {
                if (bu.getBuffer().getPath().endsWith(MAS2JProject.EXT)) {
                    //bu.getBuffer().setProperty("sidekick.parser", JasonProjectSideKickParser.ID);

                    if (Config.get().getBoolean(Config.CLOSEALL) && org.gjt.sp.jedit.jEdit.getViews().length > 0) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                // close all other files
                                Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
                                for (int i = 0; i < bufs.length; i++) {
                                    if (!bufs[i].equals(bu.getBuffer())) {
                                        org.gjt.sp.jedit.jEdit.closeBuffer(org.gjt.sp.jedit.jEdit.getViews()[0], bufs[i]);
                                    }
                                }
                            }
                        });
                    }
                }
                //if (bu.getBuffer().getPath().endsWith(MAS2JProject.AS_EXT)) {
                    //bu.getBuffer().setProperty("sidekick.parser", AgentSpeakSideKickParser.ID);
                //}
            }
        }
    }

    //static AgentSpeakSideKickParser   asskp = new AgentSpeakSideKickParser();
    //static JasonProjectSideKickParser jpskp = new JasonProjectSideKickParser();

    public void start() {
        /*
        SideKickPlugin.registerParser(asskp);
        SideKickPlugin.registerParser(jpskp);
        Log.log(Log.DEBUG, this, "Registered " + asskp);
        Log.log(Log.DEBUG, this, "Registered " + jpskp);
        */
        handleMessage(null);
    }

    /*
    public void stop() {
        SideKickPlugin.unregisterParser(asskp);
        SideKickPlugin.unregisterParser(jpskp);
    }
    */
}
