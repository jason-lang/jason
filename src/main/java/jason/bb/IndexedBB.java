package jason.bb;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;
import jason.bb.ChainBBAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Customised version of Belief Base where some beliefs are unique (with primary keys) and
 * indexed for faster access.
 *
 * <p>E.g. in a .mas2j project file:<br/>
 * <code>agents: bob beliefBaseClass jason.bb.IndexedBB("student(key,_)", "depot(_,_,_)")</code>
 * <br/>
 * The belief "student/2" has the first argument as its key, so the BB will never has
 * two students with the same key. Or, two students in the BB will have two different keys.
 * The belief "depot/3" has no key, so there will be always only one "depot" in the BB.
 *
 * When some belief with the same key than another belief in BB is added,
 * the most recent remains in the BB and the older is removed.
 *
 * @author jomi
 */
public class IndexedBB extends ChainBBAdapter {

    Map<String,Structure> indexedBels = new HashMap<String,Structure>();
    Unifier               u = new Unifier();

    public IndexedBB() {  }
    public IndexedBB(BeliefBase next) {
        super(next);
    }

    @Override
    public void init(Agent ag, String[] args) {
        for (int i=0; i<args.length; i++) {
            Structure bel = Structure.parse(args[i]);
            indexedBels.put(bel.getFunctor(), bel);
        }
    }

    // TODO: access indexes

    @Override
    public boolean add(Literal bel) {
        Structure kb = indexedBels.get(bel.getFunctor());
        if (kb != null && kb.getArity() == bel.getArity()) { // is a constrained bel?

            // find the bel in BB and eventually remove it
            u.clear();
            Literal linbb = null;
            boolean remove = false;

            Iterator<Literal> relevant = getCandidateBeliefs(bel, null);
            if (relevant != null) {
                final int kbArity = kb.getArity();
                while (relevant.hasNext() && !remove) {
                    linbb = relevant.next();

                    if (!linbb.isRule()) {
                        // check equality of all terms that are "key"
                        // if some key is different, no problem
                        // otherwise, remove the current bel
                        boolean equals = true;
                        for (int i = 0; i<kbArity; i++) {
                            Term kbt = kb.getTerm(i);
                            if (!kbt.isVar()) { // is key?
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
            }
            if (remove) {
                remove(linbb);
            }
        }
        return super.add(bel);
    }

    @Override
    public BeliefBase clone() {
        IndexedBB nbb = new IndexedBB(nextBB.clone());
        nbb.indexedBels = new HashMap<String,Structure>(this.indexedBels);
        return nbb;
    }

}
