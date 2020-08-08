package jason.asSyntax.patterns.goal;

import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;
import jason.asSyntax.directives.DefaultDirective;
import jason.asSyntax.directives.Directive;
import jason.asSyntax.directives.DirectiveProcessor;

/**
 * Implementation of the  Open-Minded Commitment pattern (see DALT 2006 paper)
 *
 * @author jomi
 */
public class OMC extends DefaultDirective implements Directive {

    static Logger logger = Logger.getLogger(OMC.class.getName());

    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Term goal = directive.getTerm(0);
            Term fail = directive.getTerm(1);
            Term motivation = directive.getTerm(2);
            Pred subDir = Pred.parsePred("bc("+goal+")");
            Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());
            String sourceNewPlans = outerContent.getASLSrc();

            // apply sub directive
            Agent newAg = sd.process(subDir, outerContent, innerContent);
            if (newAg != null) {
                // add +f : true <- .fail_goal(g).
                newAg.getPL()
                    .add(ASSyntax.parsePlan("+"+fail+" : .intend("+goal+",intention(_,[_,im(_,{+!G},_,_)|_])) <- .fail_goal(G)."))
                    .setSourceFile(sourceNewPlans);

                newAg.getPL()
                    .add(ASSyntax.parsePlan("+"+fail+" <- .drop_intention("+goal+").")) // no super goal, simply drop
                    .setSourceFile(sourceNewPlans);

                // add -m : true <- .succeed_goal(g).
                newAg.getPL()
                    .add(ASSyntax.parsePlan("-"+motivation+" <- .succeed_goal("+goal+")."))
                    .setSourceFile(sourceNewPlans);

                return newAg;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
