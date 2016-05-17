package jason.asSemantics;

import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.io.Serializable;

/**
 * Default implementation of the internal action interface (it simply returns false 
 * for the interface methods).
 * 
 * Useful to create new internal actions.
 * 
 * @author Jomi
 */
public class DefaultInternalAction implements InternalAction, Serializable {
    
    private static final long serialVersionUID = 1L;

    public boolean suspendIntention()   { return false;  }
    public boolean canBeUsedInContext() { return true;  }

    public int getMinArgs() { return 0; }
    public int getMaxArgs() { return Integer.MAX_VALUE; }
    
    protected void checkArguments(Term[] args) throws JasonException {
        if (args.length < getMinArgs() || args.length > getMaxArgs())
            throw JasonException.createWrongArgumentNb(this);            
    }
    
    public Term[] prepareArguments(Literal body, Unifier un) {
        Term[] terms = new Term[body.getArity()];
        for (int i=0; i<terms.length; i++) {
            terms[i] = body.getTerm(i).capply(un);
        }
        return terms;
    }
    
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return false;
    }
    
    public void destroy() throws Exception {
        
    }
}
