package arch;

import jason.environment.grid.Location;

import java.util.Random;

import env.WorldModel;


/**
 * Class used to model the scenario (for an agent local view)
 * 
 * @author jomi
 */
public class LocalWorldModel extends WorldModel {

    int[][]                   visited; // count the visited locations
    int                       minVisited = 0; // min value for near least visited
    
    private Random            random = new Random();
    
    //private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + LocalWorldModel.class.getName());

    public static LocalWorldModel create(int w, int h, int nbAg) {
        return new LocalWorldModel(w,h,nbAg);
    }
    
    public LocalWorldModel(int w, int h, int nbAg) {
        super(w, h, nbAg);
        
        visited = new int[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                visited[i][j] = 0;
            }
        }
    }

    @Override 
    public boolean isFree(int x, int y) {
        return super.isFree(x,y) && !hasObject(ENEMY, x, y);
    }

    public void setDepot(int x, int y) {
        if (getDepot() != null) {
            visited[getDepot().x][getDepot().y] = 0;
        }
        visited[x][y] = 10000;
        super.setDepot(x, y);
    }
    
    public int getVisited(Location l) {
        return visited[l.x][l.y];
    }

    public void incVisited(Location l) {
        incVisited(l.x,l.y);
    }
    public void incVisited(int x, int y) {
        visited[x][y] += 2;
        
        if (x > 0) visited[x-1][y]++;
        if (y > 0) visited[x][y-1]++;
        if (y > 0 && x > 0) visited[x-1][y-1]++;
        if (y+1 < getHeight()) visited[x][y+1]++;
        if (x > 0 && y+1 < getHeight()) visited[x-1][y+1]++;
        if (x+1 < getWidth()) visited[x+1][y]++;
        if (x+1 < getWidth() && y > 0) visited[x+1][y-1]++;
        if (x+1 < getWidth() && y+1 < getHeight()) visited[x+1][y+1]++;
    }
    
    /** returns the near location of x,y that was least visited */
    public Location getNearLeastVisited(int agx, int agy) {
        //int distanceToBorder = (agx < getWidth()/2 ? agx : getWidth() - agx) - 1; 
        Location agloc = new Location(agx,agy);
        
        /*
        logger.info("------");
        for (int i = 0; i < getWidth(); i++) {
            String line = "";
            for (int j = 0; j < getHeight(); j++) {
                line += visited[j][i] + " ";
            }
            logger.info(line);
        }
        */
        
        //int visitedTarget = 0;
        while (true) {

            int x = agx;
            int y = agy;
            int w = 1; 
            int dx = 0;
            int dy = 0;
            int stage = 1;//(x % 2 == 0 ? 1 : 2);
            Location better = null;
            
            while (w < getWidth()) { //( (w/2+distanceToBorder) < getWidth()) {
                switch (stage) {
                    case 1: if (dx < w) {
                                dx++;
                                break;
                            } else {
                                stage = 2;//(x % 2 == 0) ? 2 : 3; 
                            }
                    case 2: if (dy < w) {
                                dy++;
                                break;
                            } else {
                                stage = 3;//(x % 2 == 0) ? 3 : 1;
                            }
                    case 3: if (dx > 0) {
                                dx--;
                                break;
                            } else {
                                stage = 4;
                            }
                    case 4: if (dy > 0) {
                                dy--;
                                break;
                            } else {
                                stage = 1;
                                x--;
                                y--;
                                w += 2;
                            }
                }
                
                Location l = new Location(x+dx,y+dy);
                if (isFree(l) && !l.equals(agloc)) {
                    if (visited[l.x][l.y] < minVisited) { // a place better then minVisited! go there
                        return l;
                    } if (visited[l.x][l.y] == minVisited) { // a place in the minVisited level
                        if (better == null) {
                            better = l;
                        } else if (l.distance(agloc) < better.distance(agloc)) {
                            better = l;
                        } else if (l.distance(agloc) == better.distance(agloc) && random.nextBoolean()) { // to chose ramdomly equal options
                            better = l;
                        }
                    }
                }
            } // end while
            
            if (better != null) {
                return better;
            }
            minVisited++;
        }
    }
    

    /** removes enemies/gold around l */
    public void clearAgView(Location l) {
        clearAgView(l.x, l.y);
    }

    /** removes enemies/gold around x,y */
    public void clearAgView(int x, int y) {
        int e1 = ~(ENEMY + GOLD);
        
        // nw
        if (x > 0 && y > 0) {
            data[x - 1][y - 1] &= e1;
            if (view != null) view.update(x-1,y-1);
        } 
        // n
        if (y > 0) {
            data[x][y - 1] &= e1;
            if (view != null) view.update(x,y-1);
        } 
        // ne
        if (x < (getWidth() - 1) && y > 0) {
            data[x + 1][y - 1] &= e1;
            if (view != null) view.update(x+1,y-1);
        } 
        // w
        if (x > 0) {
            data[x - 1][y] &= e1;
            if (view != null) view.update(x-1,y);
        } 
        // cur
        data[x][y] &= e1;
        
        // e
        if (x < (getWidth() - 1)) {
            data[x + 1][y] &= e1;
            if (view != null) view.update(x+1,y);
        } 
        // sw
        if (x > 0 && y < (getHeight() - 1)) {
            data[x - 1][y + 1] &= e1;
            if (view != null) view.update(x-1,y+1);
        } 
        // s
        if (y < (getHeight() - 1)) {
            data[x][y + 1] &= e1;
            if (view != null) view.update(x,y+1);
        } 
        // se
        if (x < (getWidth() - 1) && y < (getHeight() - 1)) {
            data[x + 1][y + 1] &= e1;
            if (view != null) view.update(x+1,y+1);
        }
    }

}
