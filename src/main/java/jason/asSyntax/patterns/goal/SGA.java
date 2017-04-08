package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.directives.DefaultDirective;
import jason.asSyntax.directives.Directive;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Sequenced Goal Adoption pattern (see DALT 2006 paper)
 *
 * @author jomi
 */
public class SGA extends DefaultDirective implements Directive {

    static Logger logger = Logger.getLogger(SGA.class.getName());

    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Trigger trigger = ASSyntax.parseTrigger(((StringTerm)directive.getTerm(0)).getString());
            LogicalFormula context = LogExpr.parseExpr(((StringTerm)directive.getTerm(1)).getString());
            Term goal = directive.getTerm(2);

            Agent newAg = new Agent();
            newAg.initAg();

            // add t : not f__l(_) & c <- !f__g(g).
            newAg.getPL().add(ASSyntax.parsePlan(trigger+" : not f__l(_) & " +context +" <- !f__g("+goal+")."));

            // add t : f__l(_) & c <- +f__l(g).
            newAg.getPL().add(ASSyntax.parsePlan(trigger+" : f__l(_) & (" +context +") <- +f__l("+goal+")."));

            // add +!fg(g) : true <- +fl(g); !g; -fl(g)
            newAg.getPL().add(ASSyntax.parsePlan("+!f__g("+goal+") <- +f__l("+goal+"); !"+goal+"; -f__l("+goal+")."));

            // add -!fg(g) : true <- -fl(g)
            newAg.getPL().add(ASSyntax.parsePlan("-!f__g("+goal+") <- -f__l("+goal+")."));

            // add -fl(_) : fg(g) <- !fg(g)
            newAg.getPL().add(ASSyntax.parsePlan("-f__l("+goal+") : f__l("+goal+") <- !f__g("+goal+")."));

            return newAg;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive DG error.", e);
        }
        return null;
    }
}
