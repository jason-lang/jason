package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
Implementation of <b>.fort</b> (used for |& and || operators).

<p>Syntax:
<pre>
  <i>plan_body1</i> "|&" | "||" <i>plan_body2</i> ....
</pre>
</p>

|& is concurrent and: both   plan_body1 and plan_body2 have to finishes successfully
|| is concurrent or : either plan_body1 or  plan_body2 have to finishes successfully


<p>Example:
<pre>
</pre>
</p>

*/
public class fork extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new fork();
        return singleton;
    }

    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray();
    }

    @Override public int getMinArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if ( !(args[0] instanceof Atom))
            throw JasonException.createWrongArgument(this,"first argument must be 'and' or 'or'.");
    }

    @Override public boolean suspendIntention() {
        return true;
    }

    @Override public boolean canBeUsedInContext() {
        return false;
    }

    private static final Structure joinS = new Structure(".join");

    public static final Atom aAnd = new Atom("and");
    public static final Atom aOr  = new Atom("or");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        ForkData fd = new ForkData( ((Atom)args[0]).equals(aAnd)) ;

        Intention currentInt = ts.getC().getSelectedIntention();
        for (int iPlans = 1; iPlans < args.length; iPlans++) {
            Intention i = new ForkIntention(currentInt, fd);
            fd.addIntention(i);
            i.pop(); // remove the top IM, it will be introduced back later (modified)
            IntendedMeans im = (IntendedMeans)currentInt.peek().clone();

            // adds the .join in the plan
            InternalActionLiteral joinL = new InternalActionLiteral(joinS, ts.getAg());
            joinL.addTerm(new ObjectTermImpl(fd));
            PlanBody joinPB = new PlanBodyImpl(BodyType.internalAction, joinL);
            joinPB.setBodyNext(im.getCurrentStep().getBodyNext());

            // adds the argument in the plan (before join)
            PlanBody whattoadd = (PlanBody)args[iPlans].clone();
            whattoadd.add(joinPB);
            whattoadd.setAsBodyTerm(false);
            im.insertAsNextStep(whattoadd);
            im.removeCurrentStep(); // remove the .fork
            i.push(im);
            ts.getC().addIntention(i);
        }


        return true;
    }

    class ForkData {
        boolean        isAnd = true;
        Set<Intention> intentions = new HashSet<Intention>();
        int            toFinish = 0;

        public ForkData(boolean isAnd) {
            this.isAnd = isAnd;
        }

        public void addIntention(Intention i) {
            intentions.add(i);
            toFinish++;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("fork data");
            if (isAnd)
                s.append(" (and) ");
            else
                s.append(" (or) ");
            s.append(" intentions = { ");
            for (Intention i: intentions) {
                s.append(" "+i.getId());
            }
            s.append(" } waiting for "+toFinish);
            return s.toString();
        }
    }

    class ForkIntention extends Intention {
        ForkData fd;
        int forkPoint;

        ForkIntention(Intention i, ForkData fd) {
            i.copyTo(this);
            forkPoint = i.size();
            this.fd = fd;
        }

        @Override
        public boolean dropGoal(Trigger te, Unifier un) {
            boolean r = super.dropGoal(te, un);
            if (r && size() < forkPoint) {
                //System.out.println("drop "+te+" i.size = "+size()+" fork point "+forkPoint+" to f "+fd+"\n"+this);
                if (fd.toFinish > 0) { // the first intentions of the fork being dropped, keep it and ignore the rest
                    fd.toFinish = 0;
                    //System.out.println("put it back");
                    return true;
                } else {
                    clearIM();
                    //System.out.println("ignore intention");
                    return false;
                }
            }
            return r;
        }

        @Override
        public void fail(Circumstance c) {
            if (size() >= forkPoint && fd.isAnd) { // the fail is above fork, is an fork and, remove the others
                for (Intention ifo: fd.intentions) {
                    drop_intention.dropInt(c, ifo);
                }
            }
        }

        @Override
        public Pair<Event, Integer> findEventForFailure(Trigger tevent, PlanLibrary pl, Circumstance c) {
            Pair<Event, Integer> p = super.findEventForFailure(tevent, pl, c);
            if (p.getSecond() <= forkPoint) {
                if (fd.isAnd) {
                    //System.out.println("*** remove other forks");
                    fd.intentions.remove(this);
                    for (Intention ifo: fd.intentions) {
                        drop_intention.dropInt(c, ifo);
                    }
                } else {
                    //System.out.println("*** case or, do not search for fail plan below fork point");
                    return new Pair<Event, Integer>(null, p.getSecond());
                }
            }
            return p;
        }
    }
}
