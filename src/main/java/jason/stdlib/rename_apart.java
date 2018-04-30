package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;


/**
  <p>replace the variable by unused named, to avoid clash.

  <p>Examples:<ul>

  <li> <code>.rename_apart(b(X,Y,a), R)</code>: R will unifies with
  <code>b(_33_X,_34_Y,a)</code>.</li>
  </ul>

  @author Jomi
 */
public class rename_apart extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray();
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Literal newl = (Literal)args[0];
        if (newl.isVar()) { // does 1 step unification
            Literal vl = (Literal)un.get( (VarTerm)newl);
            if (vl != null)
                newl = vl;
        }
        newl = newl.makeVarsAnnon();
        return un.unifies(args[1], newl);
    }
}
