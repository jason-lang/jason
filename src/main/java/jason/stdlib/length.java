package jason.stdlib;

import java.util.Collection;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.MapTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.SetTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.length</code></b>.

  <p>Description: gets the length of strings or lists.

  <p>Parameters:<ul>
  <li>+ argument (string or list): the term whose length is to be determined.<br/>
  <li>+/- length (number).
  </ul>

  <p>Examples:<ul>
  <li> <code>.length("abc",X)</code>: <code>X</code> unifies with 3.
  <li> <code>.length([a,b],X)</code>: <code>X</code> unifies with 2.
  <li> <code>.length("a",2)</code>: false.
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.max
  @see jason.stdlib.member
  @see jason.stdlib.min
  @see jason.stdlib.sort
  @see jason.stdlib.nth
  @see jason.stdlib.sort
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

  @see jason.functions.Length function version

 */
@Manual(
        literal=".length(argument,length)",
        hint="gets the length of strings or lists",
        argsHint= {
                "the term whose length is to be determined",
                "the resulting length"
        },
        argsType= {
                "string or list",
                "number"
        },
        examples= {
                ".length(\"abc\",X): X unifies with 3",
                ".length([a,b],X): X unifies with 2",
                ".length(\"a\",2): false"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.member",
                "jason.stdlib.sort",
                "jason.stdlib.nth",
                "jason.stdlib.max",
                "jason.stdlib.min",
                "jason.stdlib.reverse",
                "jason.stdlib.difference",
                "jason.stdlib.intersection",
                "jason.stdlib.union"
        }
    )
@SuppressWarnings("serial")
public class length extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new length();
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
        Term l2 = args[1];

        NumberTerm size = getSize(args[0]);
        if (size != null) {
            return un.unifies(l2, size);
        } else {
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    public static NumberTerm getSize(Term arg) {
        if (arg.isList()) {
            ListTerm lt = (ListTerm) arg;
            return new NumberTermImpl(lt.size());
        } else if (arg.isString()) {
            StringTerm st = (StringTerm) arg;
            return new NumberTermImpl(st.getString().length());
        } else if (arg.isSet()) {
            return new NumberTermImpl(((SetTerm) arg).size());
        } else if (arg.isMap()) {
            return new NumberTermImpl(((MapTerm) arg).size());
        } else if (arg instanceof ObjectTerm o) {
            if (o.getObject() instanceof Collection) {
                return new NumberTermImpl(((Collection) o.getObject()).size());
            }
        }
        return null;
    }
}
