package jason.environment.grid;

import java.util.Random;



/**
 * Simple model for a grid world (with agents and obstacles).
 * 
 * <p>Every agent gets an identification (a integer from 0 to the number of ag - 1).
 * The relation of this identification with agent's name should be done
 * in the environment class and is application dependent.
 *  
 * <p>Every type of object in the environment is represented by a bit mask: 
 * an agent    is 000010; 
 * an obstacle is 000100; .... 
 * New types of objects should follow this pattern,
 * for example, GOLD = 8 (001000), ENEMY=16 (010000), ...  
 * A place with two object is represented by the OR between the masks:
 * an agent and a gold is 001010.
 * 
 * <p>Limitations:
 * <ul>
 * <li>The number of agents can not change dynamically</li>
 * <li>Two agents can not share the same place. More generally, 
 *     two objects with the same "mask" can not share a place.</li>
 * </ul>
 * 
 * @author Jomi
 */
public class GridWorldModel {

    // each different object is represented by having a single bit 
    // set (a bit mask is used in the model), so any power of two
    // represents different objects. Other numbers represent combinations 
    // of objects which are all located in the same cell of the grid.
    public static final int       CLEAN    = 0;
    public static final int       AGENT    = 2;
    public static final int       OBSTACLE = 4;

    protected int                 width, height;
    protected int[][]             data = null; 
    protected Location[]          agPos;
    protected GridWorldView       view;

    protected Random              random = new Random();
    

    protected GridWorldModel(int w, int h, int nbAgs) {
        width  = w;
        height = h;

        // int data
        data = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = CLEAN;
            }
        }

        agPos = new Location[nbAgs];
        for (int i = 0; i < agPos.length; i++) {
            agPos[i] = new Location(-1, -1);
        }
    }

    public void setView(GridWorldView v) {
        view = v;
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNbOfAgs() {
        return agPos.length;
    }

    public boolean inGrid(Location l) {
        return inGrid(l.x, l.y);
    }
    
    public boolean inGrid(int x, int y) {
        return y >= 0 && y < height && x >= 0 && x < width;
    }

    public boolean hasObject(int obj, Location l) {
        return inGrid(l.x, l.y) && (data[l.x][l.y] & obj) != 0;
    }
    public boolean hasObject(int obj, int x, int y) {
        return inGrid(x, y) && (data[x][y] & obj) != 0;
    }

    // gets how many objects of some kind are in the grid
    public int countObjects(int obj) {
        int c = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (hasObject(obj,i,j)) {
                    c++;
                }
            }
        }
        return c;
    }
    
    public void set(int value, int x, int y) {
        data[x][y] = value;
        if (view != null) view.update(x,y);
    }
    
    public void add(int value, Location l) {
        add(value, l.x, l.y);
    }

    public void add(int value, int x, int y) {
        data[x][y] |= value;
        if (view != null) view.update(x,y);
    }

    public void addWall(int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                add(OBSTACLE, x, y);
            }
        }
    }

    public void remove(int value, Location l) {
        remove(value, l.x, l.y);
    }

    public void remove(int value, int x, int y) {
        data[x][y] &= ~value;
        if (view != null) view.update(x,y);
    }

    public void setAgPos(int ag, Location l) {
        Location oldLoc = getAgPos(ag);
        if (oldLoc != null) {
            remove(AGENT, oldLoc.x, oldLoc.y);
        }
        agPos[ag] = l;
        add(AGENT, l.x, l.y);
    }

    public void setAgPos(int ag, int x, int y) {
        setAgPos(ag, new Location(x, y));
    }

    public Location getAgPos(int ag) {
        try {
            if (agPos[ag].x == -1)
                return null;
            else
                return (Location)agPos[ag].clone();
        } catch (Exception e) {
            return null;
        }
    }

    /** returns the agent at location l or -1 if there is not one there */
    public int getAgAtPos(Location l) {
        return getAgAtPos(l.x, l.y);
    }

    /** returns the agent at x,y or -1 if there is not one there */
    public int getAgAtPos(int x, int y) {
        for (int i=0; i<agPos.length; i++) {
            if (agPos[i].x == x && agPos[i].y == y) {
                return i;
            }
        }
        return -1;
    }

    /** returns true if the location l has no obstacle neither agent */
    public boolean isFree(Location l) {
        return isFree(l.x, l.y);
    }

    /** returns true if the location x,y has neither obstacle nor agent */
    public boolean isFree(int x, int y) {
        return inGrid(x, y) && (data[x][y] & OBSTACLE) == 0 && (data[x][y] & AGENT) == 0;
    }

    /** returns true if the location l has not the object obj */
    public boolean isFree(int obj, Location l) {
        return inGrid(l.x, l.y) && (data[l.x][l.y] & obj) == 0;     
    }
    /** returns true if the location x,y has not the object obj */
    public boolean isFree(int obj, int x, int y) {
        return inGrid(x, y) && (data[x][y] & obj) == 0;     
    }
    
    public boolean isFreeOfObstacle(Location l) {
        return isFree(OBSTACLE, l);
    }
    public boolean isFreeOfObstacle(int x, int y) {
        return isFree(OBSTACLE, x, y);
    }

    /** returns a random free location using isFree to test the availability of some possible location (it means free of agents and obstacles) */ 
    protected Location getFreePos() {
        for (int i=0; i<(getWidth()*getHeight()*5); i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            Location l = new Location(x,y);
            if (isFree(l)) {
                return l;
            }
        }
        return null; // not found
    }

    /** returns a random free location using isFree(object) to test the availability of some possible location */ 
    protected Location getFreePos(int obj) {
        for (int i=0; i<(getWidth()*getHeight()*5); i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            Location l = new Location(x,y);
            if (isFree(obj,l)) {
                return l;
            }
        }
        return null; // not found
    }
}
