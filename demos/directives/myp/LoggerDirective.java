
package myp;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.directives.DefaultDirective;
import jason.asSyntax.directives.Directive;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pattern that add .print in the begin and end of plans.

 * @author jomi
 */
public class LoggerDirective extends DefaultDirective implements Directive {

    static Logger logger = Logger.getLogger(LoggerDirective.class.getName());

    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Agent newAg = new Agent();
            newAg.initAg();
            // add .print(te) in the begin and end of the plan
            for (Plan p: innerContent.getPL()) {
                Literal print1 = Literal.parseLiteral(".print(\"Entering \","+p.getTrigger().getLiteral()+")");
                PlanBody b1 = new PlanBodyImpl(BodyType.internalAction, print1);
                p.getBody().add(0,b1);

                Literal print2 = Literal.parseLiteral(".print(\"Leaving \","+p.getTrigger().getLiteral()+")");
                PlanBody b2 = new PlanBodyImpl(BodyType.internalAction, print2);
                p.getBody().add(b2);

                newAg.getPL().add(p);
            }
            return newAg;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}

