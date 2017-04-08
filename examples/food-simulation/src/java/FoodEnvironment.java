// Environment code for project game-of-life.mas2j

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.TimeSteppedEnvironment;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FoodEnvironment extends TimeSteppedEnvironment {

    private Logger logger = Logger.getLogger("game-of-life.mas2j."+FoodEnvironment.class.getName());

    private FoodModel model;
    private FoodView view;

    private Literal lstep; // current step
    private Atom aMyPos   = new Atom("my_pos");
    private Atom aSee     = new Atom("see");
    private Atom aSmell   = new Atom("smell");

    Map<String,Integer> ag2id = new HashMap<String,Integer>();
    Map<Integer,String> id2ag = new HashMap<Integer,String>();

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(new String[] { "1000" } ); // set step timeout
        setOverActionsPolicy(OverActionsPolicy.ignoreSecond);

        model = new FoodModel(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        view  = new FoodView(model, this);
        model.setView(view);
    }

    @Override
    public Collection<Literal> getPercepts(String agName) {
        // if the agent is not in the map, add it and update its perception
        if (ag2id.get(agName) == null) {
            updateAgPercept(addAg2IdMap(agName));
        }
        return super.getPercepts(agName);
    }

    private static int lastUsedId = -1;
    private synchronized int addAg2IdMap(String agName) {
        lastUsedId++;
        ag2id.put(agName,lastUsedId);
        id2ag.put(lastUsedId, agName);
        return lastUsedId;
    }

    // TODO: implement simultaneous attack

    @Override
    public boolean executeAction(String agName, Structure action) {
        String actId = action.getFunctor();
        if (actId.equals("pause"))
            return true;

        int agId = ag2id.get(agName);
        try {
            if (actId.equals("eat")) {
                model.eat(agId);
            } else if (actId.equals("move")) {
                int x = (int)(((NumberTerm)(action.getTerm(0))).solve());
                int y = (int)(((NumberTerm)(action.getTerm(1))).solve());
                model.move(agId, x, y);
            } else if (actId.equals("attack")) {
                int x = (int)(((NumberTerm)(action.getTerm(0))).solve());
                int y = (int)(((NumberTerm)(action.getTerm(1))).solve());
                model.attack(agId, x, y);
                //int o = model.getAgAtPos(x, y);
                //System.out.println("Attac "+agName+"->" +o+id2ag.get(o)+ " "+isEating(id2ag.get(o)));
            } else if (actId.equals("random_move")) {
                model.randomMove(agId);
            } else {
                logger.warning("Unknown action: "+action);
            }
        } catch (Exception e) {}
        return true;
    }

    @Override
    protected int requiredStepsForAction(String agName, Structure action) {
        if (action.getFunctor().equals("eat")) {
            return 3; // eat takes 3 steps
        }
        return super.requiredStepsForAction(agName, action);
    }

    @Override
    protected void stepStarted(int step) {
        //logger.info("start step "+step);
        lstep = ASSyntax.createLiteral("step", ASSyntax.createNumber(step+1));
    }

    long sum = 0;
    List<Double> strategicValues  = new ArrayList<Double>();
    List<Double> reputationValues = new ArrayList<Double>();


    @Override
    protected void stepFinished(int step, long time, boolean timeout) {
        if (step % 100 == 0) {
            long mean = (step > 0 ? sum / step : 0);
            logger.info(String.format("step %10d finished in %3d ms. Str/Var/Att %7.0f %7.0f %7d", step, mean, +model.getStrengthMean(),model.getVarianceOfStrength(),model.getAttackCounter() ));
            //logger.info(String.format("  Strategic Str/Var/Att %7.0f", getStrength("strategic") ));
            //logger.info(String.format("  Normative Str/Var/Att %7.0f", getStrength("normative") ));
            //logger.info(String.format("  Reputation Str/Var/Att %7.0f", getStrength("reputation") ));

            if (view != null) {
                view.addSerie("strategic", getData(strategicValues));
                view.addSerie("reputation", getData(reputationValues));
            }
        }
        sum += time;
        strategicValues.add(getStrength("strategic"));
        reputationValues.add(getStrength("reputation"));
    }


    double getStrength(String typeOfAg) {
        double sum = 0;
        double q   = 0;
        for (int i=0; i<getNbAgs(); i++) {
            if (id2ag.get(i).startsWith(typeOfAg)) {
                sum += model.getAgStrength(i);
                q++;
            }
        }
        if (q > 0)
            return sum / q;
        else
            return 0;
    }

    private double[][] getData(List<Double> values) {
        double[][] r = new double[2][values.size()];
        int i = 0;
        for (double v: values) {
            r[0][i] = i;
            r[1][i] = v;
            i++;
        }
        return r;
    }

    @Override
    protected void updateAgsPercept() {
        for (int i = 0; i < model.getNbOfAgs(); i++) {
            updateAgPercept(i);
        }
    }

    void updateAgPercept(int ag) {
        //if (ag < 0 || ag >= model.getNbOfAgs()) return;
        String name = id2ag.get(ag);
        if (name != null) {
            updateAgPercept(name, ag);
        } else {
            logger.warning("Can not give perception to "+ag+" because it is no registered!");
        }
    }

    void updateAgPercept(String agName, int ag) {
        clearPercepts(agName);

        Location l = model.getAgPos(ag);
        Literal lpos = ASSyntax.createLiteral("pos",
                                              ASSyntax.createNumber(ag),
                                              ASSyntax.createNumber(l.x),
                                              ASSyntax.createNumber(l.y));
        addPercept(agName, lpos);

        Literal lstrength = ASSyntax.createLiteral("strength",
                            ASSyntax.createNumber(model.getAgStrength(ag)));
        addPercept(agName, lstrength);

        testAg(agName, l.x - 1, l.y);
        testAg(agName, l.x + 1, l.y);
        testAg(agName, l.x, l.y + 1);
        testAg(agName, l.x, l.y - 1);

        testFood(agName, ag, aMyPos,  l.x, l.y);
        testFood(agName, ag, aSee, l.x - 1, l.y);
        testFood(agName, ag, aSee, l.x + 1, l.y);
        testFood(agName, ag, aSee, l.x, l.y + 1);
        testFood(agName, ag, aSee, l.x, l.y - 1);

        testFood(agName, ag, aSmell, l.x,   l.y - 2);
        testFood(agName, ag, aSmell, l.x,   l.y + 2);
        testFood(agName, ag, aSmell, l.x+1, l.y - 1);
        testFood(agName, ag, aSmell, l.x+1, l.y + 1);
        testFood(agName, ag, aSmell, l.x+2, l.y);
        testFood(agName, ag, aSmell, l.x-2, l.y);
        testFood(agName, ag, aSmell, l.x-1, l.y - 1);
        testFood(agName, ag, aSmell, l.x-1, l.y + 1);

        addPercept(agName, lstep);

        int a = model.isAttacked(ag);
        if (a >= 0) {
            addPercept(agName, Literal.parseLiteral("attacked("+a+","+id2ag.get(a)+")"));
        }
    }

    void testFood(String agName, int ag, Atom where, int x, int y) {
        if (model.hasObject(FoodModel.FOOD, x, y)) {
            Literal f = ASSyntax.createLiteral("food",
                                               ASSyntax.createNumber(x),
                                               ASSyntax.createNumber(y),
                                               where,
                                               ASSyntax.createNumber(model.getFoodOwner(x, y)));
            addPercept(agName, f);
        }
    }

    private static Atom aEating = new Atom("eating");
    private static Atom aMoving = new Atom("moving");
    void testAg(String agName, int x, int y) {
        int other = model.getAgAtPos(x, y);
        if (other >= 0) {
            Literal f = ASSyntax.createLiteral("agent", // seeing
                                               ASSyntax.createNumber(other),
                                               ASSyntax.createNumber(x),
                                               ASSyntax.createNumber(y),
                                               ASSyntax.createNumber(model.getAgStrength(other)));

            if (isEating(agName)) {
                f.addTerm(aEating);
            } else {
                f.addTerm(aMoving);
            }
            addPercept(agName, f);
        }
    }

    private boolean isEating(String agName) {
        Structure actInSchedule = getActionInSchedule(agName);
        return actInSchedule != null && actInSchedule.getFunctor().equals("eat");
    }
}
