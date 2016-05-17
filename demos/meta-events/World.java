// Environment code for project meta-events.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

public class World extends Environment {

    private Logger logger = Logger.getLogger("meta-events.mas2j."+World.class.getName());

    Literal lb = ASSyntax.createLiteral("battery", new Atom("low"));
    
    @Override
    public void init(String[] args) {
        super.init(args);
        
        new Thread() {
            public void run() {
                try {
                    while (isRunning()) {
                        Thread.sleep(2000);
                        addPercept( lb );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
    
    @Override
    public boolean executeAction(String agName, Structure action) {
        
        if (action.toString().equals("move")) {
            logger.info("moving");
            try { Thread.sleep(200); } catch (Exception e) {}
        } else if (action.toString().equals("plug")) {
            logger.info("plug. charging....");
            new Thread() {
                public void run() {
                    try { Thread.sleep(2000); } catch (Exception e) {}
                    logger.info("charged");
                    removePercept(lb);                    
                };
            }.start();
        } else if (action.toString().equals("unplug")) {
            logger.info("unplug");
        }
        return true;
    }

}

