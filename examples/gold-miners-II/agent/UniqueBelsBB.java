package agent;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.bb.DefaultBeliefBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Customised version of Belief Base where some beliefs are unique (with primary keys).
 * <p>E.g.:<br/>
 * <code>beliefBaseClass agent.UniqueBelsBB("student(key,_)", "depot(_,_,_)")</code>
 * <br/>
 * The belief "student/2" has the first argument as its key, so the BB will never has
 * two students with the same key. Or, two students in the BB will have two different keys.
 * The belief "depot/3" has no key, so there will be always only one "depot" in the BB.
 *
 * @author jomi
 */
public class UniqueBelsBB extends DefaultBeliefBase {
    //static private Logger logger = Logger.getLogger(UniqueBelsBB.class.getName());

    Map<String,Literal> uniqueBels = new HashMap<String,Literal>();
    Unifier             u = new Unifier();

    public void init(Agent ag, String[] args) {
        for (int i=0; i<args.length; i++) {
            Literal arg = Literal.parseLiteral(args[i]);
            uniqueBels.put(arg.getFunctor(), arg);
        }
    }

    @Override
    public boolean add(Literal bel) {
        Literal kb = uniqueBels.get(bel.getFunctor());
        if (kb != null && kb.getArity() == bel.getArity()) {

            // find the bel in BB and eventually remove it
            u.clear();
            Literal linbb = null;
            boolean remove = false;

            Iterator<Literal> relevant = getCandidateBeliefs(bel, null);
            if (relevant != null) {
                while (relevant.hasNext() && !remove) {
                    linbb = relevant.next();

                    boolean equals = true;
                    for (int i = 0; i<kb.getArity(); i++) {
                        Term kbt = kb.getTerm(i);
                        if (!kbt.isVar()) {
                            if (!u.unifies(bel.getTerm(i), linbb.getTerm(i))) {
                                equals = false;
                                break;
                            }
                        }
                    }
                    if (equals) {
                        remove = true;
                    }
                }
            }
            if (remove) {
                remove(linbb);
            }
        }
        return super.add(bel);
    }

}
