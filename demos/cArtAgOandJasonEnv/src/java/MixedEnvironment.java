import java.util.logging.Logger;

import jaca.CartagoEnvironment;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;


public class MixedEnvironment extends Environment {

    private Logger logger = Logger.getLogger("CartJasonEnv."+MixedEnvironment.class.getName());

    private CartagoEnvironment cartagoEnv;

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        addPercept(Literal.parseLiteral("percept(demo)"));
        startCartago(args);
    }

    public void startCartago(String[] args) {
        cartagoEnv = new CartagoEnvironment();
        cartagoEnv.init(args);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action);
        addPercept(Literal.parseLiteral("percept(a2done)"));
        return true;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
        if (cartagoEnv != null)
            cartagoEnv.stop();
    }

}
