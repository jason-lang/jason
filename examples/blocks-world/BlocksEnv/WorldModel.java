package BlocksEnv;

import jason.environment.grid.GridWorldModel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

public class WorldModel extends GridWorldModel {

    public static final int   BLOCK = 32;
    public static final int   TABLE = 64;
    private Logger            logger   = Logger.getLogger("BlocksWorld.mas2j." + WorldModel.class.getName());


    private String                     id = "WorldModel";
    private List<Stack<String>>        stackList = new LinkedList<Stack<String>>();
    private String[][]                 names;

    public static int         GHeight = 0;
    public static int         GWidth = 0;

    // singleton pattern
    protected static WorldModel model = null;

    private WorldModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
    }

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

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String toString() {
        return id;
    }

    public String getName(int x, int y) {
        return names[x][y];
    }

    public List<Stack<String>> getStacks() {
        return stackList;
    }

    /** Actions **/

    boolean move(String a, String b, List<String> adds, List<String> dels) throws Exception {
        logger.info("Moving: "+a+" on top of "+b);
        Stack<String> aS = null;
        Stack<String> bS = null;
        for (Stack<String> s : stackList) {
            if (s.peek().equals(a))
                aS = s;
            if (s.peek().equals(b))
                bS = s;
        }
        if (b.equals("table")) {
            bS = new Stack<String>();
            bS.add("table");
            stackList.add(0,bS);
        }
        if (aS==null || bS==null) {
            logger.info("Didn't find one of the blocks on top of a stack");
            return false;
        }
        adds.add("on("+aS.peek()+","+bS.peek()+")");
        bS.push(aS.pop());
        dels.add("on("+bS.peek()+","+aS.peek()+")");
        if (aS.peek().equals("table")) {
            stackList.remove(aS);
        }
        modelToGrid();
        if (view != null)
            view.update();

        return true;
    }


    /** world with gold and obstacles */
    static WorldModel world1() throws Exception {
        GWidth =20;
        GHeight=10;
        WorldModel model = WorldModel.create(GWidth, GHeight, 0);
        model.names = new String[GWidth][GHeight];

        Stack<String>  s1 = new Stack<String>();
        //s1.addAll(Arrays.asList(new String[] {"table", "c", "b", "a"}));
        s1.push("table");
        s1.push("c");
        s1.push("b");
        s1.push("a");
        model.stackList.add(s1);
        Stack<String>  s2 = new Stack<String>();
        s2.push("table");
        s2.push("e");
        s2.push("d");
        model.stackList.add(s2);
        Stack<String>  s3 = new Stack<String>();
        s3.push("table");
        s3.push("g");
        s3.push("f");
        model.stackList.add(s3);
        model.modelToGrid();
        return model;
    }

    static WorldModel world2() throws Exception {
        GWidth =20;
        GHeight=10;
        WorldModel model = WorldModel.create(GWidth, GHeight, 0);
        model.names = new String[GWidth][GHeight];
        model.modelToGrid();
        return model;
    }

    static WorldModel world3() throws Exception {
        GWidth=50;
        GHeight=10;
        WorldModel model = WorldModel.create(GWidth, GHeight, 0);
        model.names = new String[GWidth][GHeight];
        model.modelToGrid();
        return model;
    }

    static WorldModel world4() throws Exception {
        GWidth =50;
        GHeight=10;
        WorldModel model = WorldModel.create(GWidth, GHeight, 0);
        model.names = new String[GWidth][GHeight];
        model.modelToGrid();
        return model;
    }

    void modelToGrid() {
        for (int i=0; i<GWidth; i++) {
            for (int j=0; j<GHeight-1; j++) {
                model.data[i][j] = 0;
                model.names[i][j] = "";
            }
            model.data[i][GHeight-1] = TABLE;
            model.names[i][GHeight-1] = "table";
        }
        int i=0;
        for (Stack<String> s : stackList) {
            for (int j=1; j<s.size(); j++) {
                model.data[i*2+1][GHeight-j-1] = BLOCK;
                model.names[i*2+1][GHeight-j-1] = s.get(j);
            }
            i++;
        }
    }
}
