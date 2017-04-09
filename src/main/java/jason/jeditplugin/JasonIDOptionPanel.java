package jason.jeditplugin;


import jason.util.ConfigGUI;

import org.gjt.sp.jedit.AbstractOptionPane;

public class JasonIDOptionPanel extends AbstractOptionPane  {

    static ConfigGUI gui = new ConfigGUI();

    public JasonIDOptionPanel() {
        super("Jason");
    }

    protected void _init() {
        addComponent(gui.getJasonConfigPanel());
    }

    protected void _save() {
        gui.save();
    }
}
