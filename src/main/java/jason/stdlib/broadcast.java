package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.broadcast</code></b>.

  <p>Description: broadcasts a message to all known agents.

  <p>Parameters:<ul>

  <li>+ ilf (atom): the illocutionary force of the message (tell,
  achieve, ...). <br/>

  <li>+ message (literal): the content of the message.<br/>

  </ul>

  <p>Example:<ul>

  <li> <code>.broadcast(tell,value(10))</code>: sends <code>value(10)</code>
  as a "tell" message to all known agents in the society.</li>

  </ul>

  @see jason.stdlib.send
  @see jason.stdlib.my_name

*/
public class broadcast extends DefaultInternalAction {

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args);
        if (!args[0].isAtom()) {
            throw JasonException.createWrongArgument(this,"illocutionary force argument must be an atom");
        }
    }

    @Override
    public boolean canBeUsedInContext() {
        return false;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Term ilf = args[0];
        Term pcnt = args[1];

        Message m = new Message(ilf.toString(), ts.getUserAgArch().getAgName(), null, pcnt);
        ts.getUserAgArch().broadcast(m);
        return true;
    }

}
