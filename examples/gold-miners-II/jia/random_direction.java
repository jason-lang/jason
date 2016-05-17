package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

import java.util.Random;

import env.WorldModel;

import arch.MinerArch;



public class random_direction extends DefaultInternalAction {
    Random rnd = new Random();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            String sAction = null;

            WorldModel model = ((MinerArch)ts.getUserAgArch()).getModel();

            NumberTerm agx = (NumberTerm) terms[0];
            NumberTerm agy = (NumberTerm) terms[1];
            int iagx = (int) agx.solve();
            int iagy = (int) agy.solve();
            int itox = -1;
            int itoy = -1;
            while (!model.isFree(itox, itoy)) {
                switch (rnd.nextInt(4)) {
                case 0:
                    itox = iagx - 1;
                    sAction = "left";
                    break;
                case 1:
                    itox = iagx + 1;
                    sAction = "right";
                    break;
                case 2:
                    itoy = iagy - 1;
                    sAction = "up";
                    break;
                case 3:
                    itoy = iagy + 1;
                    sAction = "down";
                    break;
                }
            }
            return un.unifies(terms[2], new Atom(sAction));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
