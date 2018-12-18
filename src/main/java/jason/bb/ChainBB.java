package jason.bb;

import jason.asSemantics.Agent;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * A wrapper for a chain of belief bases customisations.
 *
 * The arguments are the belief bases in the chain
 *
 * jason.bb.ChainBB( bb1, bb2, bb3, ... )
 *
 * where each BB is bbclass(bb parameters)
 *
 *  e.g.:
 *  <pre>
 *  agents:
        bob beliefBaseClass jason.bb.ChainBB(
              jason.bb.TextPersistentBB,
              jason.bb.IndexedBB("student(key,_)", "depot(_,_,_)")
            );
 *  </pre>
 */
public class ChainBB extends ChainBBAdapter {

    private static Logger logger = Logger.getLogger(ChainBB.class.getSimpleName());

    @Override
    public void init(Agent ag, String[] args) {
        setNext(null); // remove DefaultBB
        try {
            // create all chain BBs
            for (String s: args) {
                Structure bbs = Structure.parse(s);
                BeliefBase bb = (ChainBBAdapter) Class.forName(bbs.getFunctor()).newInstance();
                addInChain(bb);
            }

            // init BB
            ChainBBAdapter bb = getNextAdapter();
            for (String s: args) {
                // translate the terms to String[]
                Structure bbs = Structure.parse(s);
                String[] bbargs = new String[bbs.getArity()];
                int i = 0;
                if (bbs.hasTerm()) {
                    for (Term t: bbs.getTerms()) {
                        bbargs[i++] = t.isString() ? ((StringTerm)t).getString(): t.toString();
                    }
                }
                bb.init(ag, bbargs);
                bb = bb.getNextAdapter();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating ChainBB",e);
        }
    }

    /** add a new BB at the end of the chain */
    public void addInChain(BeliefBase bb) {
        if (getNextAdapter() == null) {
            setNext(bb);
        } else {
            ChainBBAdapter last = getNextAdapter();
            while (last.getNextAdapter() != null) {
                last = last.getNextAdapter();
            }
            last.setNext(bb);
        }
    }

    @SuppressWarnings("rawtypes")
    public List<Class> getChainClasses() {
        List<Class> r = new ArrayList<Class>();
        ChainBBAdapter c = getNextAdapter();
        while (c != null) {
            r.add(c.getClass());
            if (c.nextBB != null && c.nextBB instanceof DefaultBeliefBase)
                r.add(c.nextBB.getClass());
            c = c.getNextAdapter();
        }
        return r;
    }
}
