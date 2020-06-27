package jason.stdlib;

import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.union(S1,S2,S3)</code></b>.

  <p>Description: S3 is the union of the sets S1 and S2 (represented by lists).
  The result set is sorted.

  <p>Parameters:<ul>
  <li>+ arg[0] (a list).<br/>
  <li>+ arg[1] (a list).<br/>
  <li>+/- arg[2]: the result of the union.
  </ul>

  <p>Examples:<ul>
  <li> <code>.union("[a,b,c]","[b,e]",X)</code>: <code>X</code> unifies with "[a,b,c,e]".
  <li> <code>.union("[a,b,a,c]","[f,e]",X)</code>: <code>X</code> unifies with "[a,b,c,e,f]".
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.sort
  @see jason.stdlib.substring
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse
  @see jason.stdlib.difference
  @see jason.stdlib.intersection
*/
@Manual(
        literal=".union(arg0,arg1,result)",
        hint="the sorted set that represents the union of two sets",
        argsHint= {
                "the first set",
                "the set to be joined with the first",
                "the result of the union"
        },
        argsType= {
                "list",
                "list",
                "list"
        },
        examples= {
                ".union(\"[a,b,c]\",\"[b,e]\",X): X unifies with \"[a,b,c,e]\"",
                ".union(\"[a,b,a,c]\",\"[f,e]\",X): X unifies with \"[a,b,c,e,f]\""
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.length",
                "jason.stdlib.member",
                "jason.stdlib.sort",
                "jason.stdlib.substring",
                "jason.stdlib.nth",
                "jason.stdlib.max",
                "jason.stdlib.min",
                "jason.stdlib.reverse",
                "jason.stdlib.difference",
                "jason.stdlib.intersection"
        }
    )
@SuppressWarnings("serial")
public class union extends difference { // to inherit checkArguments

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new union();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return un.unifies(args[2], ((ListTerm)args[0]).union( (ListTerm)args[1]) );
    }
}
