package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.directives.DefaultDirective;
import jason.asSyntax.directives.Directive;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.bb.BeliefBase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the  Maintenance Goal pattern (see DALT 2006 paper)
 *
 * @author jomi
 */
public class MG extends DefaultDirective  implements Directive {

    static Logger logger = Logger.getLogger(MG.class.getName());

    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());
            Pred subDir;
            if (directive.getArity() > 1) {
                subDir = Pred.parsePred(directive.getTerm(1).toString());
            } else {
                subDir = Pred.parsePred("bc("+goal+")");
            }
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());

            // apply sub directive
            Agent newAg = sd.process(subDir, outerContent, innerContent);
            if (newAg != null) {
                // add bel g
                Literal ig = goal.copy();
                ig.addAnnot(BeliefBase.TPercept);
                newAg.addInitialBel(goal);

                // add -g : true <- !g.
                newAg.getPL().add(ASSyntax.parsePlan("-"+goal+" <- !"+goal+"."));

                return newAg;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
