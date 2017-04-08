import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/** class that implements the Model of the Food application */
public class FoodModel extends GridWorldModel {

    public static final int FOOD  = 16; // represent a cell with food

    public static final int INITIAL_STR = 40;
    public static final int FOOD_NUTRITIVE_VALUE = 20;
    public static final int MOVING_COST = 1;
    public static final int ATTACK_COST = 4;

    //private Logger logger = Logger.getLogger(FoodModel.class.getName());

    int[] strengths;
    int[] attacked;
    int[][] owner; // the owner (agent id) of each food
    private int attackCount = 0;

    public FoodModel(int size, int ags, int foods) {
        super(size, size, ags);

        strengths = new int[ags];
        attacked = new int[ags];
        owner = new int[size][size];

        // create agents
        for (int i=0; i<ags; i++) {
            setAgPos(i, getFreePos());
            strengths[i] = INITIAL_STR;
        }

        // set attackers
        clearAttackers();

        // create food
        for (int i=0; i<foods; i++) {
            add(FOOD, getFreePos(FOOD));
        }
        setFoodOwners();
    }

    protected void clearAttackers() {
        for (int i=0; i<attacked.length; i++) {
            attacked[i] = -1;
        }
    }

    protected void setFoodOwners() {
        for (int x=0; x<getWidth(); x++) {
            for (int y=0; y<getHeight(); y++) {
                setFoodOwner(x, y);
            }
        }
    }

    private void setFoodOwner(int x, int y) {
        if (hasObject(FOOD, x, y)) {
            // find an agent around
            int ag;
            // food pos
            ag = getAgAtPos(x, y);
            if (ag >= 0) {
                owner[x][y] = ag;
                return;
            }
            // up
            ag = getAgAtPos(x, y-1);
            if (ag >= 0) {
                owner[x][y] = ag;
                return;
            }
            // down
            ag = getAgAtPos(x, y+1);
            if (ag >= 0) {
                owner[x][y] = ag;
                return;
            }
            // left
            ag = getAgAtPos(x-1, y);
            if (ag >= 0) {
                owner[x][y] = ag;
                return;
            }
            // right
            ag = getAgAtPos(x+1, y);
            if (ag >= 0) {
                owner[x][y] = ag;
                return;
            }
        }
        owner[x][y] = -1;
    }

    public int getFoodOwner(int x, int y) {
        return owner[x][y];
    }

    public boolean eat(int ag) {
        Location l = getAgPos(ag);
        return eat(ag, l.x, l.y);
    }

    private boolean eat(int ag, int x, int y) {
        if (hasObject(FOOD, x, y)) {
            //if (ag != owner[x][y] && owner[x][y] != -1)
            //    System.out.println(ag+" eating "+x+","+y+" "+owner[x][y]);
            remove(FOOD, x, y);
            owner[x][y] = -1;
            strengths[ag] += FOOD_NUTRITIVE_VALUE;
            Location l = getFreePos(FOOD);
            add(FOOD, l);
            setFoodOwner(l.x, l.y);
            return true;
        }
        return false;
    }

    public boolean move(int ag, int x, int y) {
        //if (strengths[ag] < MOVING_COST)
        //  return false;

        Location l = getAgPos(ag);
        strengths[ag] -= MOVING_COST;

        // should go right
        if (l.x < x && isFree(l.x+1,l.y)) {
            setAgPos(ag, l.x+1, l.y);
            return true;
        }
        // should go left
        if (l.x > x && isFree(l.x-1,l.y)) {
            setAgPos(ag, l.x-1, l.y);
            return true;
        }
        // should go up
        if (l.y > y && isFree(l.x,l.y-1)) {
            setAgPos(ag, l.x, l.y-1);
            return true;
        }
        // should go down
        if (l.y < y && isFree(l.x,l.y+1)) {
            setAgPos(ag, l.x, l.y+1);
            return true;
        }
        return false;
    }

    public boolean randomMove(int ag) {
        Location l = getAgPos(ag);
        Location nl = null;
        for (int i=0; i<4; i++) {
            switch (random.nextInt(4)) {
            case 0:
                nl = new Location(l.x+1, l.y);
                break;
            case 1:
                nl = new Location(l.x-1, l.y);
                break;
            case 2:
                nl = new Location(l.x, l.y+1);
                break;
            case 3:
                nl = new Location(l.x, l.y-1);
                break;
            }
            if (isFree(nl) && isFree(FOOD, nl)) {
                return move(ag,nl.x,nl.y);
            }
        }
        return false;
    }

    public boolean attack(int ag, int x, int y) {
        //if (strengths[ag] < ATTACK_COST)
        //  return false;

        strengths[ag] -= ATTACK_COST;

        int other = getAgAtPos(x, y);
        if (other < 0)
            return false;

        attackCount++;
        if (strengths[ag] > strengths[other]) {
            strengths[other] -= ATTACK_COST;
            attacked[other] = ag;

            Location agl = getAgPos(ag);

            // move food of position
            if (isFree(FOOD, agl) && hasObject(FOOD, x, y)) {
                remove(FOOD, x, y);
                owner[x][y] = -1;
                add(FOOD, agl);
                return true;
            }
            //eat(ag, x, y);
        }
        return false;
    }

    public int isAttacked(int ag) {
        return attacked[ag];
    }

    public int getAgStrength(int ag) {
        return strengths[ag];
    }

    public double getStrengthMean() {
        double sum = 0;
        for (int i=0; i<strengths.length; i++) {
            sum += strengths[i];
        }
        return sum / strengths.length;
    }

    public double getVarianceOfStrength() {
        double mean = getStrengthMean();
        double sum = 0;
        for (int i=0; i<strengths.length; i++) {
            sum = sum + Math.pow((double)strengths[i] - mean, 2);
        }
        return Math.sqrt(sum / strengths.length);
    }

    public int getAttackCounter() {
        return attackCount;
    }

}
