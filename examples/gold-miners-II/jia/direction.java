package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.Random;
import java.util.logging.Level;

import arch.LocalWorldModel;
import arch.MinerArch;
import busca.Nodo;

/**
 * Gives the direction (up, down, left, right) towards some location.
 * Uses A* for this task.
 *
 * @author jomi
 */
public class direction extends DefaultInternalAction {

    int[]      actionsOrder = { 1, 2, 3, 4 }; // initial order of actions
    Random     random = new Random();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = "skip";

            LocalWorldModel model = ((MinerArch)ts.getUserAgArch()).getModel();

            int iagx = (int)((NumberTerm)terms[0]).solve();
            int iagy = (int)((NumberTerm)terms[1]).solve();
            int itox = (int)((NumberTerm)terms[2]).solve();
            int itoy = (int)((NumberTerm)terms[3]).solve();

            if (model.inGrid(itox,itoy)) {
                // destination should be a free place
                while (!model.isFreeOfObstacle(itox,itoy) && itox > 0) itox--;
                while (!model.isFreeOfObstacle(itox,itoy) && itox < model.getWidth()) itox++;

                Location from = new Location(iagx, iagy);
                Location to   = new Location(itox, itoy);

                // randomly change the place of two actions in actionsOrder
                int i1 = random.nextInt(4);
                int i2 = random.nextInt(4);
                int temp = actionsOrder[i2];
                actionsOrder[i2] = actionsOrder[i1];
                actionsOrder[i1] = temp;

                if (actionsOrder[0] + actionsOrder[1] + actionsOrder[2] + actionsOrder[3] != 10) {
                    ts.getLogger().warning("****** lost action!!!!!");
                }

                Search astar    = new Search(model, from, to, actionsOrder, true);
                Nodo   solution = astar.search();
                if (solution != null) {
                    String ac = astar.firstAction(solution);
                    if (ac != null) {
                        sAction = ac;
                    }
                } else {
                    ts.getLogger().info("No route from "+from+" to "+to+"!"+"\n"+model);
                }
            }
            return un.unifies(terms[4], new Atom(sAction));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "direction error: "+e, e);
        }
        return false;
    }
}

