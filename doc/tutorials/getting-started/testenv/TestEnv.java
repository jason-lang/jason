// Environment code for project testeenv.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

public class TestEnv extends jason.environment.Environment {

    private Logger logger = Logger.getLogger("testeenv.mas2j."+TestEnv.class.getName());

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        if (action.getFunctor().equals("burn")) {
            addPercept(Literal.parseLiteral("fire"));
            return true;
        } else {
            logger.info("executing: "+action+", but not implemented!");
            return false;
        }
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

