package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IMCondition;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

public class done extends succeed_goal {

    private boolean resultSuspend = true;

    @Override
    public boolean suspendIntention() {
        return resultSuspend;
    }

    @Override public int getMinArgs() {
        return 0;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length > 0 && !args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Trigger g;
        if (args.length > 0) {
            g = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);
        } else {
            // get goal from the context => the near goal
            Intention i = ts.getC().getSelectedIntention();
            if (i.getGIntention() != null)
                i = i.getGIntention();
            g = i.peek().getTrigger().clone();
        }

        drop(ts, new IMCondition() {
            public boolean test(Trigger t, Unifier u) {
                return u.unifies(g, t);
            }
            @Override
            public Trigger getTrigger() {
                return g;
            }
        }, un);
        return true;
    }

    @Override
    public void drop(TransitionSystem ts, IMCondition c, Unifier un) throws Exception {
        Circumstance C = ts.getC();
        Unifier bak = un.clone();


        // dropping the current intention?
        Intention i = C.getSelectedIntention();
        if (i != null) {
            int r = dropIntention(i, c, ts, un);
            //ts.getLogger().info("** rem 1 : "+r+" for "+i);
            if (r > 0) {
                i = i.getGIntention();
                if (i == null) {
                    // the g-plan is done, do not suspend it
                    resultSuspend = false;
                    // TODO: stop e-plan siblings, if they branch above the removed IM
                } else {
                    // e-plan case
                    un = bak.clone();
                    r = dropIntention(i, c, ts, un);
                    //ts.getLogger().info("** rem 2 : "+r+" for "+i);
                    if (r > 0)
                        // the e-plan is done
                        C.dropIntention(i); // remove it from whatever it is
                    if (r == 1) {
                        //ts.getLogger().info("back to "+i);
                        C.addRunningIntention(i); // and resume
                    }
                }
            }
        }
    }

}
