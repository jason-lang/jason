// Environment code for project water_jugs.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import jason.stdlib.map.create;
import jason.asSyntax.parser.*;

import java.util.logging.*;

public class Jugs extends Environment {

    private Logger logger = Logger.getLogger("water_jugs.mas2j."+Jugs.class.getName());

    int j5 = 0; // water in jug 5l
    int j3 = 0; // water in jug 3l

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        createPerpects();
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("doing "+action);
        try {
            if (action.getFunctor().equals("fill")) {
                int j = (int) ((NumberTerm)action.getTerm(0)).solve();
                if (j == 5)
                    j5 = 5;
                if (j == 3)
                    j3 = 3;
            } else if (action.getFunctor().equals("empty")) {
                int j = (int) ((NumberTerm)action.getTerm(0)).solve();
                if (j == 5)
                    j5 = 0;
                if (j == 3)
                    j3 = 0;
            } else if (action.getFunctor().equals("pour")) {
                int jo = (int) ((NumberTerm)action.getTerm(0)).solve();
                int jd = (int) ((NumberTerm)action.getTerm(0)).solve();
                if (jo == 5) {
                    int v = Math.min(j5, 3-j3);
                    j5 -= v;
                    j3 += v;
                }
                if (jo == 3) {
                    int v = Math.min(j3, 5-j5);
                    j3 -= v;
                    j5 += v;
                }
            }
            createPerpects();
            informAgsEnvironmentChanged();
            return true; // the action was executed with success
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void createPerpects() {
        super.clearPercepts();
        try {
            addPercept(ASSyntax.parseLiteral("jug(5,"+j5+")"));
            addPercept(ASSyntax.parseLiteral("jug(3,"+j3+")"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

