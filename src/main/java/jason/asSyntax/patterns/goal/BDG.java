package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Backtracking Declarative Goal pattern (see DALT 2006 paper)
 *
 * @author jomi
 */
public class BDG extends DG {

    static Logger logger = Logger.getLogger(BDG.class.getName());

    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            // apply DG in the inner plans
            Agent newAg = super.process(directive, outerContent, innerContent);
            if (newAg != null) {

                Term goal = directive.getTerm(0);

                // add -!g : true <- !g.
                newAg.getPL().add(ASSyntax.parsePlan("-!"+goal+" <- !"+goal+"."));

                return newAg;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
