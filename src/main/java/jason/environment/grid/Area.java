package jason.environment.grid;

import java.io.Serializable;

public final class Area implements Serializable {
    public final Location tl, br;
    
    public Area(int topLeftX, int topLeftY, int bottonRightX, int bottonRightY) {
        tl = new Location(topLeftX, topLeftY);
        br = new Location(bottonRightX, bottonRightY);
    }
    
    public Area(Location topLeft, Location bottonRight) {
        tl = topLeft;
        br = bottonRight;
    }
    
    public boolean contains(Location l) {
        return l.x >= tl.x && l.x <= br.x && l.y >= tl.y && l.y <= br.y;
    }
    
    public Location center() {
        return new Location( (tl.x + br.x)/2, (tl.y + br.y)/2);
    }

    /** @deprecated renamed to chebyshevDistanceToBorder */
    public int distanceMaxBorder(Location l) {
        return chebyshevDistanceToBorder(l);
    }
    
    /** returns the minimal distance from <i>l</i> to the border of the area */    
    public int chebyshevDistanceToBorder(Location l) {
        if (contains(l)) 
            return 0;
        int x = Math.min( Math.abs(tl.x - l.x), Math.abs(br.x - l.x));
        int y = Math.min( Math.abs(tl.y - l.y), Math.abs(br.y - l.y));
        return Math.max(x,y);
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + tl.x + tl.y;
        result = PRIME * result + br.x + br.y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;
        final Area other = (Area)obj;
        if (! tl.equals(other.tl)) return false;
        if (! br.equals(other.br)) return false;
        return true;
    }
    
    public Object clone() {
        return new Area(tl,br);
    }
    
    public String toString() {
        return (tl + ":" + br);
    }
}
