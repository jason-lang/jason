package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class setof extends DefaultInternalAction {

    @Override public int getMinArgs() { return 3; }
    @Override public int getMaxArgs() { return 3; }

    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        return body.getTermsArray(); // we do not need to clone nor to apply for this internal action
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (! (args[1] instanceof LogicalFormula))
            throw JasonException.createWrongArgument(this,"second argument must be a formula");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term var = args[0];
        LogicalFormula logExpr = (LogicalFormula)args[1];
        Set<Term> all = new TreeSet<Term>();
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un);
        while (iu.hasNext()) {
            all.add(var.capply(iu.next()));
        }
        return un.unifies(args[2], setToList(all));
    }
    
    // copy the set to a new list
    private ListTerm setToList(Set<Term> set) {
        ListTerm result = new ListTermImpl();
        ListTerm tail = result;
        for (Term t: set)
            tail = tail.append(t.clone());
        return result;
    }
}
