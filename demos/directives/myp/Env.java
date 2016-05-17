// Environment code for project goalpattern.mas2j

package myp;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

public class Env extends jason.environment.Environment {

    //private Logger logger = Logger.getLogger("goalpattern.mas2j."+Env.class.getName());

    @Override
    public boolean executeAction(String ag, Structure action) {
        if (action.getFunctor().equals("action2")) {
            addPercept(Literal.parseLiteral("g"));
        }
        return true;
    }
}

