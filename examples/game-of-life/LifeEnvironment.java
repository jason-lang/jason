// Environment code for project game-of-life.mas2j

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.grid.Location;

import java.util.logging.Logger;

public class LifeEnvironment extends jason.environment.TimeSteppedEnvironment {

    private Logger logger = Logger.getLogger("game-of-life.mas2j."+LifeEnvironment.class.getName());

    private LifeModel model;
    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(new String[] { "3000" } ); // set step timeout
        setOverActionsPolicy(OverActionsPolicy.ignoreSecond);
        model = new LifeModel(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        model.setView(new LifeView(model, this));
        updateAgsPercept();
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        String actId = action.getFunctor();
        if (actId.equals("skip")) 
            return true;
        
        int ag = getAgIdBasedOnName(agName);
        if (actId.equals("die"))
            model.dead(ag);
        else if (actId.equals("live"))
            model.alive(ag);
        
        //updateNeighbors(ag);
        return true;
    }

    @Override
    protected void stepStarted(int step) {
        //logger.info("start step "+step);
    }
    
    private long sum = 0;
    
    @Override
    protected void stepFinished(int step, long time, boolean timeout) {
        long mean = (step > 0 ? sum / step : 0);
        logger.info("step "+step+" finished in "+time+" ms. mean = "+mean);
        sum += time;
    }
    
    int getAgIdBasedOnName(String agName) {
        return (Integer.parseInt(agName.substring(4))) - 1;
    }
    
    @Override
    protected void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    void updateNeighbors(int ag) {        
        Location l = model.getAgPos(ag);
        updateAgPercept(model.getAgId(l.x - 1, l.y - 1));
        updateAgPercept(model.getAgId(l.x - 1, l.y));
        updateAgPercept(model.getAgId(l.x - 1, l.y + 1));
        updateAgPercept(model.getAgId(l.x, l.y - 1));
        updateAgPercept(model.getAgId(l.x, l.y));
        updateAgPercept(model.getAgId(l.x, l.y + 1));
        updateAgPercept(model.getAgId(l.x + 1, l.y - 1));
        updateAgPercept(model.getAgId(l.x + 1, l.y));
        updateAgPercept(model.getAgId(l.x + 1, l.y + 1));
    }
    
    void updateAgPercept(int ag) {
        //if (ag < 0 || ag >= model.getNbOfAgs()) return;
        String name = "cell" + (ag + 1);
        updateAgPercept(name, ag);
    }

    void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        
        // its location
        Location l = model.getAgPos(ag);

        // how many alive neighbours
        int alive = 0;
        if (model.isAlive(l.x - 1, l.y - 1)) alive++;
        if (model.isAlive(l.x - 1, l.y))     alive++;
        if (model.isAlive(l.x - 1, l.y + 1)) alive++;
        if (model.isAlive(l.x, l.y - 1))     alive++;
        //if (model.isAlive(l.x, l.y))         alive++;
        if (model.isAlive(l.x, l.y + 1))     alive++;
        if (model.isAlive(l.x + 1, l.y - 1)) alive++;
        if (model.isAlive(l.x + 1, l.y))     alive++;
        if (model.isAlive(l.x + 1, l.y + 1)) alive++;
        Literal lAlive = ASSyntax.createLiteral("alive_neighbors", ASSyntax.createNumber(alive));
        addPercept(agName, lAlive);        
        addPercept(agName, ASSyntax.createLiteral("step", ASSyntax.createNumber(getStep())));
    }
}
