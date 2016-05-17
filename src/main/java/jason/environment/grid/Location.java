package jason.environment.grid;

import java.io.Serializable;

public final class Location implements Serializable {
    public int x, y;
    
    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /** calculates the Manhattan distance between two points */
    public int distanceManhattan(Location l) {
        return Math.abs(x - l.x) + Math.abs(y - l.y);
    }
    /** calculates the Manhattan distance between two points */
    public int distance(Location l) {
        return Math.abs(x - l.x) + Math.abs(y - l.y);
    }

    /** calculates the Euclidean distance between two points */
    public double distanceEuclidean(Location l) {
        return Math.sqrt(Math.pow(x - l.x, 2) + Math.pow(y - l.y, 2));
    }

    /** returns the chessboard king (or Chebyshev) distance between two locations : max( |this.x - l.x| , |this.y - l.y|) */
    public int distanceChebyshev(Location l) {
        return Math.max( Math.abs(this.x - l.x) , Math.abs(this.y - l.y));
    }
    /** @deprecated renamed to distanceChessboard */
    public int maxBorder(Location l) {
        return Math.max( Math.abs(this.x - l.x) , Math.abs(this.y - l.y));
    }

    public boolean isInArea(Location tl, Location br) {
        return x >= tl.x && x <= br.x && y >= tl.y && y <= br.y;
    }
    public boolean isInArea(Area a) {
        return a.contains(this);
    }
    public boolean isNeigbour(Location l) {
        return 
            distance(l) == 1 ||
            equals(l) ||
            Math.abs(x - l.x) == 1 && Math.abs(y - l.y) == 1;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + x;
        result = PRIME * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;
        final Location other = (Location) obj;
        if (x != other.x) return false;
        if (y != other.y) return false;
        return true;
    }
    
    public Object clone() {
        return new Location(x,y);
    }
    
    public String toString() {
        return (x + "," + y);
    }
}
