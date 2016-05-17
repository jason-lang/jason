import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.Random;

/** class that implements the Model of the Game of Life application */
public class LifeModel extends GridWorldModel {
    
    public static final int LIFE  = 16; // represent a cell with life

    //private Logger logger = Logger.getLogger(LifeModel.class.getName());
    
    Random random = new Random();
    
    public LifeModel(int size, int density) {
        super(size, size, size*size);

        // initial agents' state (alive or dead)
        try {
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    int ag = getAgId(i,j);
                    setAgPos(ag, i, j);
                    if (random.nextInt(100) < density) {
                        alive(ag);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Location getAgPos(int ag) {
        return new Location(ag / getWidth(), ag % getWidth());
    }
    
    int getAgId(int x, int y) {
        return x*getWidth() + y;
    }
    
    void alive(int ag) {
        add(LIFE, getAgPos(ag));
    }
    
    boolean isAlive(int ag) {
        return hasObject(LIFE, getAgPos(ag));
    }
    
    boolean isAlive(int x, int y) {
        return hasObject(LIFE, x, y);
    }

    void dead(int ag) {
        remove(LIFE, getAgPos(ag));
    }  
}
