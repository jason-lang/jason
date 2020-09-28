package jason.stdlib;

import java.util.Iterator;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.belief(Bel)</code></b>.

<p>Description: gets beliefs in BB (excluding rules or inferred beliefs).
Different from ordinary belief query that uses logical consequence to be verified, this internal action considers only the set of beliefs in the BB.

<p>Parameters:
<ul>
<li>+/- belief (literal): the belief to be searched.</li>

</ul>

  @see jason.stdlib.asserta
  @see jason.stdlib.assertz
  @see jason.stdlib.relevant_rules

*/
public class belief extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 1;
    }
    @Override public int getMaxArgs() {
        return 1;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final Literal pattern = (Literal)args[0];
        final Iterator<Literal> i = ts.getAg().getBB().getCandidateBeliefs(pattern, un);
        if (i != null) {
            return new Iterator<Unifier>() {
                Unifier c = null; // the current response (which is an unifier)

                { find(); }

                public boolean hasNext() {
                    return c != null;
                }

                public Unifier next() {
                    Unifier b = c;
                    find(); // find next response
                    return b;
                }

                void find() {
                    while (i.hasNext()) {
                        Literal l = i.next();
                        if (!l.isRule()) {
                            c = (Unifier)un.clone();
                            if (c.unifies(pattern, l)) {
                                return;
                            }
                        } else if ( ( (Rule)l ).getBody().toString().equals("true") ) {
                            c = (Unifier)un.clone();
                            if (c.unifies(pattern, l)) {
                                return;
                            }
                        }
                    }

                    c = null; // no member is found,
                }
            };
        }
        return false;
    }
}

