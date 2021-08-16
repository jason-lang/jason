package mylib;

import jason.*;
import jason.runtime.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class my_create_ag extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // use Settings to add initial beliefs and goals for the new agent
        // (as used in the .mas2j project file)
        Settings s = new Settings();
        s.addOption(Settings.INIT_BELS, "b(10),b(20)");
        s.addOption(Settings.INIT_GOALS, "a");

        // RuntimeServices provides services to create agents in the current platform (Local, JADE, JaCaMo, ...)
        RuntimeServices rs = RuntimeServicesFactory.get();
        String name = "anotherBob";
        name = rs.createAgent(name, "bob.asl", null, null, null, s, ts.getAg());
        rs.startAgent(name);

        // everything ok, so returns true
        return true;
    }
}
