package mining;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import mining.MiningPlanet.Move;

public class WorldModel extends GridWorldModel {

    public static final int   GOLD  = 16;
    public static final int   DEPOT = 32;
    public static final int   ENEMY = 64;

    Location                  depot;
    Set<Integer>              agWithGold;  // which agent is carrying gold
    int                       goldsInDepot   = 0;
    int                       initialNbGolds = 0;

    private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModel.class.getName());

    private String            id = "WorldModel";
    
    // singleton pattern
    protected static WorldModel model = null;
    
    synchronized public static WorldModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new WorldModel(w, h, nbAgs);
        }
        return model;
    }
    
    public static WorldModel get() {
        return model;
    }
    
    public static void destroy() {
        model = null;
    }

    private WorldModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        agWithGold = new HashSet<Integer>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String toString() {
        return id;
    }
    
    public Location getDepot() {
        return depot;
    }

    public int getGoldsInDepot() {
        return goldsInDepot;
    }
    
    public boolean isAllGoldsCollected() {
        return goldsInDepot == initialNbGolds;
    }
    
    public void setInitialNbGolds(int i) {
        initialNbGolds = i;
    }
    
    public int getInitialNbGolds() {
        return initialNbGolds;
    }

    public boolean isCarryingGold(int ag) {
        return agWithGold.contains(ag);
    }

    public void setDepot(int x, int y) {
        depot = new Location(x, y);
        data[x][y] = DEPOT;
    }

    public void setAgCarryingGold(int ag) {
        agWithGold.add(ag);
    }
    public void setAgNotCarryingGold(int ag) {
        agWithGold.remove(ag);
    }

    /** Actions **/

    boolean move(Move dir, int ag) throws Exception {
        Location l = getAgPos(ag);
        switch (dir) {
        case UP:
            if (isFree(l.x, l.y - 1)) {
                setAgPos(ag, l.x, l.y - 1);
            }
            break;
        case DOWN:
            if (isFree(l.x, l.y + 1)) {
                setAgPos(ag, l.x, l.y + 1);
            }
            break;
        case RIGHT:
            if (isFree(l.x + 1, l.y)) {
                setAgPos(ag, l.x + 1, l.y);
            }
            break;
        case LEFT:
            if (isFree(l.x - 1, l.y)) {
                setAgPos(ag, l.x - 1, l.y);
            }
            break;
        }
        return true;
    }

    boolean pick(int ag) {
        Location l = getAgPos(ag);
        if (hasObject(WorldModel.GOLD, l.x, l.y)) {
            if (!isCarryingGold(ag)) {
                remove(WorldModel.GOLD, l.x, l.y);
                setAgCarryingGold(ag);
                return true;
            } else {
                logger.warning("Agent " + (ag + 1) + " is trying the pick gold, but it is already carrying gold!");
            }
        } else {
            logger.warning("Agent " + (ag + 1) + " is trying the pick gold, but there is no gold at " + l.x + "x" + l.y + "!");
        }
        return false;
    }

    boolean drop(int ag) {
        Location l = getAgPos(ag);
        if (isCarryingGold(ag)) {
            if (l.equals(getDepot())) {
                goldsInDepot++;
                logger.info("Agent " + (ag + 1) + " carried a gold to depot!");
            } else {
                add(WorldModel.GOLD, l.x, l.y);
            }
            setAgNotCarryingGold(ag);
            return true;
        }
        return false;
    }

    /*
    public void clearAgView(int agId) {
        clearAgView(getAgPos(agId).x, getAgPos(agId).y);
    }

    public void clearAgView(int x, int y) {
        int e1 = ~(ENEMY + ALLY + GOLD);
        if (x > 0 && y > 0) {
            data[x - 1][y - 1] &= e1;
        } // nw
        if (y > 0) {
            data[x][y - 1] &= e1;
        } // n
        if (x < (width - 1) && y > 0) {
            data[x + 1][y - 1] &= e1;
        } // ne

        if (x > 0) {
            data[x - 1][y] &= e1;
        } // w
        data[x][y] &= e1; // cur
        if (x < (width - 1)) {
            data[x + 1][y] &= e1;
        } // e

        if (x > 0 && y < (height - 1)) {
            data[x - 1][y + 1] &= e1;
        } // sw
        if (y < (height - 1)) {
            data[x][y + 1] &= e1;
        } // s
        if (x < (width - 1) && y < (height - 1)) {
            data[x + 1][y + 1] &= e1;
        } // se
    }
    */

    
    /** no gold/no obstacle world */
    static WorldModel world1() throws Exception {
        WorldModel model = WorldModel.create(21, 21, 4);
        model.setDepot(5, 7);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    /** world with gold, no obstacle */
    static WorldModel world2() throws Exception {
        WorldModel model = WorldModel.create(35, 35, 4);
        model.setId("Scenario 4");
        model.setDepot(5, 27);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 3, 20);
        model.setAgPos(3, 20, 20);
        model.add(WorldModel.GOLD, 20, 13);
        model.add(WorldModel.GOLD, 15, 20);
        model.add(WorldModel.GOLD, 1, 1);
        model.add(WorldModel.GOLD, 3, 5);
        model.add(WorldModel.GOLD, 24, 24);
        model.add(WorldModel.GOLD, 20, 20);
        model.add(WorldModel.GOLD, 20, 21);
        model.add(WorldModel.GOLD, 20, 22);
        model.add(WorldModel.GOLD, 20, 23);
        model.add(WorldModel.GOLD, 20, 24);
        model.add(WorldModel.GOLD, 19, 20);
        model.add(WorldModel.GOLD, 19, 21);
        model.add(WorldModel.GOLD, 34, 34);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

    /** world with gold and obstacles */
    static WorldModel world3() throws Exception {
        WorldModel model = WorldModel.create(35, 35, 4);
        model.setId("Scenario 5");
        model.setDepot(16, 16);
        model.setAgPos(0, 1, 0);
        model.setAgPos(1, 20, 0);
        model.setAgPos(2, 6, 26);
        model.setAgPos(3, 20, 20);
        model.add(WorldModel.GOLD, 20, 13);
        model.add(WorldModel.GOLD, 15, 20);
        model.add(WorldModel.GOLD, 1, 1);
        model.add(WorldModel.GOLD, 3, 5);
        model.add(WorldModel.GOLD, 24, 24);
        model.add(WorldModel.GOLD, 20, 20);
        model.add(WorldModel.GOLD, 20, 21);
        model.add(WorldModel.GOLD, 2, 22);
        model.add(WorldModel.GOLD, 2, 12);
        model.add(WorldModel.GOLD, 19, 2);
        model.add(WorldModel.GOLD, 14, 4);
        model.add(WorldModel.GOLD, 34, 34);

        model.add(WorldModel.OBSTACLE, 12, 3);
        model.add(WorldModel.OBSTACLE, 13, 3);
        model.add(WorldModel.OBSTACLE, 14, 3);
        model.add(WorldModel.OBSTACLE, 15, 3);
        model.add(WorldModel.OBSTACLE, 18, 3);
        model.add(WorldModel.OBSTACLE, 19, 3);
        model.add(WorldModel.OBSTACLE, 20, 3);
        model.add(WorldModel.OBSTACLE, 14, 8);
        model.add(WorldModel.OBSTACLE, 15, 8);
        model.add(WorldModel.OBSTACLE, 16, 8);
        model.add(WorldModel.OBSTACLE, 17, 8);
        model.add(WorldModel.OBSTACLE, 19, 8);
        model.add(WorldModel.OBSTACLE, 20, 8);

        model.add(WorldModel.OBSTACLE, 12, 32);
        model.add(WorldModel.OBSTACLE, 13, 32);
        model.add(WorldModel.OBSTACLE, 14, 32);
        model.add(WorldModel.OBSTACLE, 15, 32);
        model.add(WorldModel.OBSTACLE, 18, 32);
        model.add(WorldModel.OBSTACLE, 19, 32);
        model.add(WorldModel.OBSTACLE, 20, 32);
        model.add(WorldModel.OBSTACLE, 14, 28);
        model.add(WorldModel.OBSTACLE, 15, 28);
        model.add(WorldModel.OBSTACLE, 16, 28);
        model.add(WorldModel.OBSTACLE, 17, 28);
        model.add(WorldModel.OBSTACLE, 19, 28);
        model.add(WorldModel.OBSTACLE, 20, 28);

        model.add(WorldModel.OBSTACLE, 3, 12);
        model.add(WorldModel.OBSTACLE, 3, 13);
        model.add(WorldModel.OBSTACLE, 3, 14);
        model.add(WorldModel.OBSTACLE, 3, 15);
        model.add(WorldModel.OBSTACLE, 3, 18);
        model.add(WorldModel.OBSTACLE, 3, 19);
        model.add(WorldModel.OBSTACLE, 3, 20);
        model.add(WorldModel.OBSTACLE, 8, 14);
        model.add(WorldModel.OBSTACLE, 8, 15);
        model.add(WorldModel.OBSTACLE, 8, 16);
        model.add(WorldModel.OBSTACLE, 8, 17);
        model.add(WorldModel.OBSTACLE, 8, 19);
        model.add(WorldModel.OBSTACLE, 8, 20);

        model.add(WorldModel.OBSTACLE, 32, 12);
        model.add(WorldModel.OBSTACLE, 32, 13);
        model.add(WorldModel.OBSTACLE, 32, 14);
        model.add(WorldModel.OBSTACLE, 32, 15);
        model.add(WorldModel.OBSTACLE, 32, 18);
        model.add(WorldModel.OBSTACLE, 32, 19);
        model.add(WorldModel.OBSTACLE, 32, 20);
        model.add(WorldModel.OBSTACLE, 28, 14);
        model.add(WorldModel.OBSTACLE, 28, 15);
        model.add(WorldModel.OBSTACLE, 28, 16);
        model.add(WorldModel.OBSTACLE, 28, 17);
        model.add(WorldModel.OBSTACLE, 28, 19);
        model.add(WorldModel.OBSTACLE, 28, 20);

        model.add(WorldModel.OBSTACLE, 13, 13);
        model.add(WorldModel.OBSTACLE, 13, 14);

        model.add(WorldModel.OBSTACLE, 13, 16);
        model.add(WorldModel.OBSTACLE, 13, 17);

        model.add(WorldModel.OBSTACLE, 13, 19);
        model.add(WorldModel.OBSTACLE, 14, 19);

        model.add(WorldModel.OBSTACLE, 16, 19);
        model.add(WorldModel.OBSTACLE, 17, 19);

        model.add(WorldModel.OBSTACLE, 19, 19);
        model.add(WorldModel.OBSTACLE, 19, 18);

        model.add(WorldModel.OBSTACLE, 19, 16);
        model.add(WorldModel.OBSTACLE, 19, 15);

        model.add(WorldModel.OBSTACLE, 19, 13);
        model.add(WorldModel.OBSTACLE, 18, 13);

        model.add(WorldModel.OBSTACLE, 16, 13);
        model.add(WorldModel.OBSTACLE, 15, 13);

        // labirinto
        model.add(WorldModel.OBSTACLE, 2, 32);
        model.add(WorldModel.OBSTACLE, 3, 32);
        model.add(WorldModel.OBSTACLE, 4, 32);
        model.add(WorldModel.OBSTACLE, 5, 32);
        model.add(WorldModel.OBSTACLE, 6, 32);
        model.add(WorldModel.OBSTACLE, 7, 32);
        model.add(WorldModel.OBSTACLE, 8, 32);
        model.add(WorldModel.OBSTACLE, 9, 32);
        model.add(WorldModel.OBSTACLE, 10, 32);
        model.add(WorldModel.OBSTACLE, 10, 31);
        model.add(WorldModel.OBSTACLE, 10, 30);
        model.add(WorldModel.OBSTACLE, 10, 29);
        model.add(WorldModel.OBSTACLE, 10, 28);
        model.add(WorldModel.OBSTACLE, 10, 27);
        model.add(WorldModel.OBSTACLE, 10, 26);
        model.add(WorldModel.OBSTACLE, 10, 25);
        model.add(WorldModel.OBSTACLE, 10, 24);
        model.add(WorldModel.OBSTACLE, 10, 23);
        model.add(WorldModel.OBSTACLE, 2, 23);
        model.add(WorldModel.OBSTACLE, 3, 23);
        model.add(WorldModel.OBSTACLE, 4, 23);
        model.add(WorldModel.OBSTACLE, 5, 23);
        model.add(WorldModel.OBSTACLE, 6, 23);
        model.add(WorldModel.OBSTACLE, 7, 23);
        model.add(WorldModel.OBSTACLE, 8, 23);
        model.add(WorldModel.OBSTACLE, 9, 23);
        model.add(WorldModel.OBSTACLE, 2, 29);
        model.add(WorldModel.OBSTACLE, 2, 28);
        model.add(WorldModel.OBSTACLE, 2, 27);
        model.add(WorldModel.OBSTACLE, 2, 26);
        model.add(WorldModel.OBSTACLE, 2, 25);
        model.add(WorldModel.OBSTACLE, 2, 24);
        model.add(WorldModel.OBSTACLE, 2, 23);
        model.add(WorldModel.OBSTACLE, 2, 29);
        model.add(WorldModel.OBSTACLE, 3, 29);
        model.add(WorldModel.OBSTACLE, 4, 29);
        model.add(WorldModel.OBSTACLE, 5, 29);
        model.add(WorldModel.OBSTACLE, 6, 29);
        model.add(WorldModel.OBSTACLE, 7, 29);
        model.add(WorldModel.OBSTACLE, 7, 28);
        model.add(WorldModel.OBSTACLE, 7, 27);
        model.add(WorldModel.OBSTACLE, 7, 26);
        model.add(WorldModel.OBSTACLE, 7, 25);
        model.add(WorldModel.OBSTACLE, 6, 25);
        model.add(WorldModel.OBSTACLE, 5, 25);
        model.add(WorldModel.OBSTACLE, 4, 25);
        model.add(WorldModel.OBSTACLE, 4, 26);
        model.add(WorldModel.OBSTACLE, 4, 27);
        model.setInitialNbGolds(model.countObjects(WorldModel.GOLD));
        return model;
    }

}
