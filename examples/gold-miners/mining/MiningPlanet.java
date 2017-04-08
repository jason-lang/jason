package mining;

// Environment code for project jasonTeamSimLocal.mas2j

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MiningPlanet extends jason.environment.Environment {

    private Logger logger = Logger.getLogger("jasonTeamSimLocal.mas2j." + MiningPlanet.class.getName());

    WorldModel  model;
    WorldView   view;

    int     simId    = 3; // type of environment
    int     nbWorlds = 3;

    int     sleep    = 0;
    boolean running  = true;
    boolean hasGUI   = true;

    public static final int SIM_TIME = 60;  // in seconds

    Term                    up       = Literal.parseLiteral("do(up)");
    Term                    down     = Literal.parseLiteral("do(down)");
    Term                    right    = Literal.parseLiteral("do(right)");
    Term                    left     = Literal.parseLiteral("do(left)");
    Term                    skip     = Literal.parseLiteral("do(skip)");
    Term                    pick     = Literal.parseLiteral("do(pick)");
    Term                    drop     = Literal.parseLiteral("do(drop)");

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    @Override
    public void init(String[] args) {
        hasGUI = args[2].equals("yes");
        sleep  = Integer.parseInt(args[1]);
        initWorld(Integer.parseInt(args[0]));
    }

    public int getSimId() {
        return simId;
    }

    public void setSleep(int s) {
        sleep = s;
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        boolean result = false;
        try {
            if (sleep > 0) {
                Thread.sleep(sleep);
            }

            // get the agent id based on its name
            int agId = getAgIdBasedOnName(ag);

            if (action.equals(up)) {
                result = model.move(Move.UP, agId);
            } else if (action.equals(down)) {
                result = model.move(Move.DOWN, agId);
            } else if (action.equals(right)) {
                result = model.move(Move.RIGHT, agId);
            } else if (action.equals(left)) {
                result = model.move(Move.LEFT, agId);
            } else if (action.equals(skip)) {
                result = true;
            } else if (action.equals(pick)) {
                result = model.pick(agId);
            } else if (action.equals(drop)) {
                result = model.drop(agId);
                view.udpateCollectedGolds();
            } else {
                logger.info("executing: " + action + ", but not implemented!");
            }
            if (result) {
                updateAgPercept(agId);
                return true;
            }
        } catch (InterruptedException e) {
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error executing " + action + " for " + ag, e);
        }
        return false;
    }

    private int getAgIdBasedOnName(String agName) {
        return (Integer.parseInt(agName.substring(5))) - 1;
    }

    public void initWorld(int w) {
        simId = w;
        try {
            switch (w) {
            case 1:
                model = WorldModel.world1();
                break;
            case 2:
                model = WorldModel.world2();
                break;
            case 3:
                model = WorldModel.world3();
                break;
            default:
                logger.info("Invalid index!");
                return;
            }
            clearPercepts();
            addPercept(Literal.parseLiteral("gsize(" + simId + "," + model.getWidth() + "," + model.getHeight() + ")"));
            addPercept(Literal.parseLiteral("depot(" + simId + "," + model.getDepot().x + "," + model.getDepot().y + ")"));
            if (hasGUI) {
                view = new WorldView(model);
                view.setEnv(this);
                view.udpateCollectedGolds();
            }
            updateAgsPercept();
            informAgsEnvironmentChanged();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
        }
    }

    public void endSimulation() {
        addPercept(Literal.parseLiteral("end_of_simulation(" + simId + ",0)"));
        informAgsEnvironmentChanged();
        if (view != null) view.setVisible(false);
        WorldModel.destroy();
    }

    private void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    private void updateAgPercept(int ag) {
        updateAgPercept("miner" + (ag + 1), ag);
    }

    private void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);
        // its location
        Location l = model.getAgPos(ag);
        addPercept(agName, Literal.parseLiteral("pos(" + l.x + "," + l.y + ")"));

        if (model.isCarryingGold(ag)) {
            addPercept(agName, Literal.parseLiteral("carrying_gold"));
        }

        // what's around
        updateAgPercept(agName, l.x - 1, l.y - 1);
        updateAgPercept(agName, l.x - 1, l.y);
        updateAgPercept(agName, l.x - 1, l.y + 1);
        updateAgPercept(agName, l.x, l.y - 1);
        updateAgPercept(agName, l.x, l.y);
        updateAgPercept(agName, l.x, l.y + 1);
        updateAgPercept(agName, l.x + 1, l.y - 1);
        updateAgPercept(agName, l.x + 1, l.y);
        updateAgPercept(agName, l.x + 1, l.y + 1);
    }


    private void updateAgPercept(String agName, int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;
        if (model.hasObject(WorldModel.OBSTACLE, x, y)) {
            addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",obstacle)"));
        } else {
            if (model.hasObject(WorldModel.GOLD, x, y)) {
                addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",gold)"));
            }
            if (model.hasObject(WorldModel.ENEMY, x, y)) {
                addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",enemy)"));
            }
            if (model.hasObject(WorldModel.AGENT, x, y)) {
                addPercept(agName, Literal.parseLiteral("cell(" + x + "," + y + ",ally)"));
            }
        }
    }

}
