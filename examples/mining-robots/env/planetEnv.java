package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;

import java.util.Random;

public class planetEnv extends Environment {

    public final int gridSize;
    public final int middle;

    public final PlanetCell[][] planet;
    public final boolean[][] resourcemap;

    public final int X = 0;
    public final int Y = 1;

    public int col1[];
    public int col2[];
    public int col3[];


    public int boss[];

    public Random random = new Random(System.currentTimeMillis());

    public Literal col1Pos;
    public Literal col2Pos;
    public Literal col3Pos;
    public Literal resC1;
    public Literal resC2;
    public Literal resC3;

    public int rid;
//        public List rstore = new LinkedList();

    public Literal r1fin = Literal.parseLiteral("enough(1)");
    public Literal r2fin = Literal.parseLiteral("enough(2)");
    public Literal finished = Literal.parseLiteral("building_finished");

    public final String bs = new String("build_using");
    public final String mr = new String("mine");
    public final String dr = new String("drop");
    public final Term nc = Literal.parseLiteral("move_to(next_cell)");

    public int b1res;
    public int c1res;
    public int c2res;
    public int c3res;

    public GUI gui;

    public planetEnv() {

        gridSize = 30;
        middle = gridSize/2;

        planet = new PlanetCell[gridSize][gridSize];
        resourcemap = new boolean[gridSize][gridSize];

        boss = new int[2];
        boss[X] = middle;
        boss[Y] = middle;

        col1 = new int[2];
        col1[X] = middle;
        col1[Y] = middle;
        col2 = new int[2];
        col2[X] = middle;
        col2[Y] = middle;
        col3 = new int[2];
        col3[X] = middle;
        col3[Y] = middle;

        rid = 0;

        int x;
        int y;

        for(int i = 0; i<11; i++) {

            x = random.nextInt(gridSize);
            y = random.nextInt(gridSize);

            while(planet[x][y] != null || x==middle && y==middle) {

                x = random.nextInt(gridSize);
                y = random.nextInt(gridSize);
            }

            planet[x][y] = new Resource(1, 5);
            resourcemap[x][y] = true;
        }

        for(int i = 0; i<11; i++) {

            x = random.nextInt(gridSize);
            y = random.nextInt(gridSize);

            while(planet[x][y] != null || x==middle && y==middle) {

                x = random.nextInt(gridSize);
                y = random.nextInt(gridSize);
            }

            planet[x][y] = new Resource(2, 5);
            resourcemap[x][y] = true;
        }

        for(int i = 0; i<11; i++) {

            x = random.nextInt(gridSize);
            y = random.nextInt(gridSize);

            while(planet[x][y] != null || x==middle && y==middle) {

                x = random.nextInt(gridSize);
                y = random.nextInt(gridSize);
            }

            planet[x][y] = new Resource(3, 5);
            resourcemap[x][y] = true;
        }

        planet[middle][middle] = new Site();

        c1res = 1;
        c2res = 1;
        c3res = 1;

        updatePercepts("col1");
        updatePercepts("col2");
        updatePercepts("col3");

        gui = new GUI(this);

    }

    public boolean executeAction(String agent, Structure action) {
        if(action.equals(nc)) {

            if(agent.equals("col1")) {

                col1[X]++;

                if (col1[X] == gridSize) {
                    col1[X] = 0;
                    col1[Y]++;
                }
                if (col1[Y] == gridSize) {
                    col1[Y] = 0;
                }


            } else if(agent.equals("col2")) {

                col2[X]--;

                if (col2[X] == -1) {
                    col2[X] = gridSize-1;
                    col2[Y]--;
                }
                if (col2[Y] == -1) {
                    col2[Y] = gridSize-1;
                }

            } else if(agent.equals("col3")) {

                col3[Y]++;

                if (col3[Y] == gridSize) {
                    col3[Y] = 0;
                    col3[X]++;
                }
                if (col3[X] == gridSize) {
                    col3[X] = 0;
                }

            }

        } else if (action.getFunctor().equals(bs)) {

            Site s = (Site) planet[middle][middle];
            int resourceBuilt = s.build();

            Literal nr = Literal.parseLiteral("new_resource("+action.getTerm(0)+","+action.getTerm(1)+")");
            removePercept("builder",nr);

            if(resourceBuilt == 1) {
                if(s.getr1() == 0) {
                    addPercept("builder",r1fin);
                    gui.out("Resource 1 no longer needed: Requesting Resource 2.");
                }
            }
            if(resourceBuilt == 2) {
                if(s.getr2() == 0) {
                    addPercept("builder",r2fin);
                    gui.out("Resource 2 no longer needed: Requesting Resource 3.");
                }
            }
            if(resourceBuilt == 3) {
                if(s.complete()) {
                    addPercept("builder",finished);
                    gui.out("Building complete: requesting agents to return home.");
                }
            }

            gui.out("Resource "+resourceBuilt+" used, current values needed: r1 = "+s.getr1()+",r2 = "+s.getr2()+", r3 = "+s.getr3());
        } else if(action.getFunctor().equals(mr)) {

            if(agent.equals("col1")) {

                if(resourcemap[col1[X]][col1[Y]])   {
                    Resource r = (Resource) planet[col1[X]][col1[Y]];
                    r.mine();
                    c1res = r.getType();
                    gui.out("Agent A mining resource "+c1res);
                    if(r.depleted()) {
                        planet[col1[X]][col1[Y]] = null;
                        resourcemap[col1[X]][col1[Y]] = false;
                    }
                }


            } else if(agent.equals("col2")) {

                if(resourcemap[col2[X]][col2[Y]])   {
                    Resource r = (Resource) planet[col2[X]][col2[Y]];
                    r.mine();
                    c2res = r.getType();
                    gui.out("Agent B mining resource "+c2res);
                    if(r.depleted()) {
                        planet[col2[X]][col2[Y]] = null;
                        resourcemap[col2[X]][col2[Y]] = false;
                    }
                }

            } else if(agent.equals("col3")) {

                if(resourcemap[col3[X]][col3[Y]])   {
                    Resource r = (Resource) planet[col3[X]][col3[Y]];
                    r.mine();
                    c3res = r.getType();
                    gui.out("Agent C mining resource "+c3res);
                    if(r.depleted()) {
                        planet[col3[X]][col3[Y]] = null;
                        resourcemap[col3[X]][col3[Y]] = false;
                    }
                }
            }

        } else if(action.getFunctor().equals(dr)) {

            rid++;
            Literal r1store = Literal.parseLiteral("new_resource(1,"+rid+")");
            Literal r2store = Literal.parseLiteral("new_resource(2,"+rid+")");
            Literal r3store = Literal.parseLiteral("new_resource(3,"+rid+")");

            if(agent.equals("col1")) {
                Site s = (Site) planet[middle][middle];
                s.addstore(c1res);
                gui.out("Agent A dropped resource "+c1res+" at home base");
                switch(c1res) {
                case 1:
                    addPercept("builder",r1store);
                    break;
                case 2:
                    addPercept("builder",r2store);
                    break;
                case 3:
                    addPercept("builder",r3store);
                    break;
                }
            } else if(agent.equals("col2")) {
                Site s = (Site) planet[middle][middle];
                s.addstore(c2res);
                gui.out("Agent B dropped resource "+c2res+" at home base");
                switch(c2res) {
                case 1:
                    addPercept("builder",r1store);
                    break;
                case 2:
                    addPercept("builder",r2store);
                    break;
                case 3:
                    addPercept("builder",r3store);
                    break;
                }
            } else if(agent.equals("col3")) {
                Site s = (Site) planet[middle][middle];
                s.addstore(c3res);
                gui.out("Agent C dropped resource "+c3res+" at home base");
                switch(c3res) {
                case 1:
                    addPercept("builder",r1store);
                    break;
                case 2:
                    addPercept("builder",r2store);
                    break;
                case 3:
                    addPercept("builder",r3store);
                    break;
                }

            }

        } else if(action.getFunctor().equals("move_towards")) {

            int x = (new Integer(action.getTerm(0).toString())).intValue();
            int y = (new Integer(action.getTerm(1).toString())).intValue();

            if(agent.equals("col1")) {

                if (col1[X] < x)
                    col1[X]++;
                else if (col1[X] > x)
                    col1[X]--;
                if (col1[Y] < y)
                    col1[Y]++;
                else if (col1[Y] > y)
                    col1[Y]--;

            } else if(agent.equals("col2")) {

                if (col2[X] < x)
                    col2[X]++;
                else if (col2[X] > x)
                    col2[X]--;
                if (col2[Y] < y)
                    col2[Y]++;
                else if (col2[Y] > y)
                    col2[Y]--;

            } else if(agent.equals("col3")) {

                if (col3[X] < x)
                    col3[X]++;
                else if (col3[X] > x)
                    col3[X]--;
                if (col3[Y] < y)
                    col3[Y]++;
                else if (col3[Y] > y)
                    col3[Y]--;

            }

        }

        updatePercepts(agent);

        informAgsEnvironmentChanged();

        // gui.out(getPercepts("col1").toString()+getPercepts("col2").toString()+getPercepts("col3").toString()+getPercepts("builder").toString());
        gui.update();

        return true;

    }

    void updatePercepts(String agent) {
        if(agent.equals("col1")) {
            clearPercepts("col1");
            col1Pos = Literal.parseLiteral("my_pos("+col1[X]+","+col1[Y]+")");
            addPercept("col1",col1Pos);

            if(resourcemap[col1[X]][col1[Y]]) {
                Resource r = (Resource)planet[col1[X]][col1[Y]];
                int resource = r.getType();
                resC1 = Literal.parseLiteral("found("+resource+")");
                addPercept("col1",resC1);
            }
        } else if(agent.equals("col2")) {
            clearPercepts("col2");
            col2Pos = Literal.parseLiteral("my_pos("+col2[X]+","+col2[Y]+")");
            addPercept("col2",col2Pos);

            if(resourcemap[col2[X]][col2[Y]]) {
                Resource r = (Resource)planet[col2[X]][col2[Y]];
                int resource = r.getType();
                resC2 = Literal.parseLiteral("found("+resource+")");
                addPercept("col2",resC2);
            }
        } else if(agent.equals("col3")) {
            clearPercepts("col3");
            col3Pos = Literal.parseLiteral("my_pos("+col3[X]+","+col3[Y]+")");
            addPercept("col3",col3Pos);

            if(resourcemap[col3[X]][col3[Y]]) {
                Resource r = (Resource)planet[col3[X]][col3[Y]];
                int resource = r.getType();
                resC3 = Literal.parseLiteral("found("+resource+")");
                addPercept("col3",resC3);
            }
        }
    }

    public PlanetCell[][] getPlanet() {
        return planet;
    }

    public int[] geta1() {
        return col1;
    }
    public int[] geta2() {
        return col2;
    }
    public int[] geta3() {
        return col3;
    }

    public void stop() {
        super.stop();
        gui.dispose();
    }
}
