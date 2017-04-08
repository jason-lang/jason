package jason.asSyntax.patterns.goal;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
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
 * Implementation of the Declarative Goal pattern (see DALT 2006 paper)
 *
 * @author jomi
 */
public class DG extends DefaultDirective implements Directive {

    static Logger logger = Logger.getLogger(DG.class.getName());

    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        try {
            Agent newAg = new Agent();
            newAg.initAg();

            Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());

            // add +!g : g <- true.
            newAg.getPL().add(ASSyntax.parsePlan("+!"+goal+" : " +goal+"."));

            // add ?g in the end of all inner plans
            for (Plan p: innerContent.getPL()) {
                // only for +!g plans
                if (p.getTrigger().isAchvGoal()) {
                    Literal planGoal = p.getTrigger().getLiteral();
                    if (new Unifier().unifies(planGoal, goal)) { // if the plan goal unifier the pattern goal
                        PlanBody b = new PlanBodyImpl(BodyType.test, planGoal.copy()); //goal.copy());
                        p.getBody().add(b);
                    }
                }
                newAg.getPL().add(p);
            }

            // add +g : true <- .succeed_goal(g).
            newAg.getPL().add(ASSyntax.parsePlan("+"+goal+" <- .succeed_goal("+goal+")."));

            return newAg;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Directive error.", e);
        }
        return null;
    }
}
