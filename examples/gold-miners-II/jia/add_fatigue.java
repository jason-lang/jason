package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.logging.Level;

import arch.MinerArch;
import env.WorldModel;

/** adds the miner fatigue in some path length

       jia.add_fatigue(+Path_Length, [ +Golds , ] +/- New_Path)

    If "Golds" is not informed, the number of golds that the agent
    is currently carrying is used.

*/
public class add_fatigue extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
        try {
            WorldModel model = ((MinerArch)ts.getUserAgArch()).getModel();
            int agId = MinerArch.getAgId(ts.getUserAgArch().getAgName());

            int in = (int)((NumberTerm)terms[0]).solve();
            int ig = 0;
            int resultIndex = 2;
            if (terms.length == 3) {
                // use the number of golds informed as argument
                ig = (int)((NumberTerm)terms[1]).solve();
            } else {
                // use the number of golds the agent is carrying
                ig = model.getGoldsWithAg(agId);
                resultIndex = 1;
            }

            int r = (int)(in + in * model.getAgFatigue(agId, ig));
            return un.unifies(terms[resultIndex], new NumberTermImpl(r));
        } catch (Throwable e) {
            ts.getLogger().log(Level.SEVERE, "jia.add_fatigue error: "+e, e);
        }
        return false;
    }
}

