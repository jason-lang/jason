package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.reverse</code></b>.

  <p>Description: reverses strings or lists.

  <p>Parameters:<ul>
  <li>+ arg[0] (list or string): the string or list to be reversed.<br/>
  <li>+/- arg[1]: the result.
  </ul>

  <p>Examples:<ul>
  <li> <code>.reverse("abc",X)</code>: <code>X</code> unifies with "cba".
  <li> <code>.reverse("[a,b,c]",X)</code>: <code>X</code> unifies with "[c,b,a]".
  <li> <code>.reverse("[a,b,c|T]",X)</code>: <code>X</code> unifies with "[c,b,a|T]".
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.member
  @see jason.stdlib.min
  @see jason.stdlib.sort
  @see jason.stdlib.substring

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
@Manual(
        literal=".reverse(argument,result)",
        hint="reverses strings or lists",
        argsHint= {
                "the string or list to be reversed",
                "the resulting reversed string or list"
        },
        argsType= {
                "string or list",
                "string or list"
        },
        examples= {
                ".reverse(\"abc\",X): X unifies with \"cba\"",
                ".reverse(\"[a,b,c]\",X): X unifies with \"[c,b,a]\"",
                ".reverse(\"[a,b,c|T]\",X): X unifies with \"[c,b,a|T]\""
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.length",
                "jason.stdlib.member",
                "jason.stdlib.sort",
                "jason.stdlib.shuffle",
                "jason.stdlib.substring",
                "jason.stdlib.nth",
                "jason.stdlib.max",
                "jason.stdlib.min",
                "jason.stdlib.difference",
                "jason.stdlib.intersection",
                "jason.stdlib.union"
        }
    )
@SuppressWarnings("serial")
public class reverse extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new reverse();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        if (args[0].isList()) {
            // list reverse
            if (!args[1].isVar() && !args[1].isList())
                throw JasonException.createWrongArgument(this,"last argument '"+args[1]+"' must be a list or a variable.");

            return un.unifies(((ListTerm)args[0]).reverse(), args[1]);

        } else {
            // string reverse
            if (!args[1].isVar() && !args[1].isString())
                throw JasonException.createWrongArgument(this,"last argument '"+args[1]+"' must be a string or a variable.");
            if (!args[0].isString())
                throw JasonException.createWrongArgument(this,"first argument of .reverse '"+args[0]+"' must be a string or a list");

            String vl = ((StringTerm)args[0]).getString();
            return un.unifies(new StringTermImpl(new StringBuilder(vl).reverse().toString()), args[1]);
        }
    }
}
