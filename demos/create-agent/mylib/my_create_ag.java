// Internal action code for project createAgDemo.mas2j

package mylib;

import jason.*;
import jason.runtime.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class my_create_ag extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
		String name = "anotherBob";
		RuntimeServicesInfraTier rs = ts.getUserAgArch().getRuntimeServices();
		
		Settings s = new Settings();
		s.addOption(Settings.INIT_BELS, "b(10),b(20)");
		s.addOption(Settings.INIT_GOALS, "a");
		
        name = rs.createAgent(name, "bob.asl", null, null, null, s);
        rs.startAgent(name);
		
        
        // everything ok, so returns true
        return true;
    }
}

