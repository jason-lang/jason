
package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.replace(S1,S2,S3,S4)</code></b>.

  <p>Description: replaces S2 by S3 in S1, result in S4.


*/

@SuppressWarnings("serial")
public class replace extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new replace();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 4;
    }
    @Override public int getMaxArgs() {
        return 4;
    }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);

        String arg = null;
        if (args[0].isString())
            arg = ((StringTerm)args[0]).getString();
        else
            arg = args[0].toString();

        String s2 = ((StringTerm)args[1]).getString();
        String s3 = ((StringTerm)args[2]).getString();
        arg = arg.replaceAll(s2, s3);
        return un.unifies(new StringTermImpl(arg), args[3]);
    }
}

