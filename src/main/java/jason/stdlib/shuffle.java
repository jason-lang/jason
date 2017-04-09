package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;

import java.util.Collections;
import java.util.List;

/**

  <p>Internal action: <b><code>.shuffle(List,Result)</code></b>.

  <p>Description: shuffle the elements of the <i>List</i> and unifies the result in <i>Var</i>.

  <p>Parameters:<ul>

  <li>+ list: the list to be shuffled<br/>

  <li>- result (var): the list with the elements shuffled.<br/>

  </ul>

*/
public class shuffle extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (! (args[0].isList()))
            throw JasonException.createWrongArgument(this,"first argument must be a list");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        List<Term> lt = ((ListTerm)args[0]).getAsList();
        Collections.shuffle(lt);
        ListTerm ls = new ListTermImpl();
        ls.addAll(lt);
        return un.unifies(args[1], ls);
    }
}
