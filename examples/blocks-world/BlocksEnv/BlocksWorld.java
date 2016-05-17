package BlocksEnv;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlocksWorld extends jason.environment.Environment {

    private Logger logger = Logger.getLogger("BlocksWorld.mas2j" + BlocksWorld.class.getName());
    
    WorldModel  model;
    WorldView   view;
    
    int     sleep    = 0;
    boolean hasGUI   = true;
    
    @Override
    public void init(String[] args) {
        hasGUI = args[2].equals("yes"); 
        sleep  = Integer.parseInt(args[1]);
        initWorld(Integer.parseInt(args[0]));
        updateAllPercepts();
    }
    
    @Override
    synchronized public boolean executeAction(String ag, Structure action) {
        boolean result = false;
        try {
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
            
            //logger.info("Agent "+ag+" is doing: "+action);

            if (action.getFunctor().equals("move")) {
                // Location loc = new Location( (int)((NumberTerm)action.getTerm(0)).solve(),(int)((NumberTerm)action.getTerm(1)).solve());
                LinkedList<String> adds=new LinkedList<String>(), dels=new LinkedList<String>();
                result = model.move(action.getTerm(0).toString(),action.getTerm(1).toString(), adds, dels);
                if (result) {
                    for(String d: dels)
                        removePercept("agent",Literal.parseLiteral(d));
                    for(String a: adds)
                        addPercept("agent",Literal.parseLiteral(a));
                }
            } else {
                logger.info("executing: " + action + ", but it has not been implemented!");
            }
            return true;
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + ag, e);
        }
        return false;
    }

    private void updateAllPercepts() {
        clearPercepts("agent");
        for (Stack<String> s : model.getStacks()) {
            for (int i=1; i<s.size(); i++) {
                addPercept("agent", Literal.parseLiteral("on("+s.get(i)+","+s.get(i-1)+")"));
            }
        }
    }
    
    public void initWorld(int w) {
        try {
            switch (w) {
                   case 1: model = WorldModel.world1(); break;
                   case 2: model = WorldModel.world2(); break;
                   case 3: model = WorldModel.world3(); break;
            default:
                logger.info("Invalid index!");
                return;
            }
           
            if (hasGUI) {
                view = new WorldView(model);
            }   
            informAgsEnvironmentChanged();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
        }
    }
}
