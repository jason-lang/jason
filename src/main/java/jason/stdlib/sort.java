package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Collections;
import java.util.List;

/**

  <p>Internal action: <b><code>.sort</code></b>.

  <p>Description: sorts a list of terms. The "natural" order for each type of
  terms is used. Between different types of terms, the following order is
  used:<br>

  numbers &lt; strings &lt; lists &lt; literals (by negation, arity, functor, terms, annotations) &lt; variables

  <p>Parameters:<ul>
  <li>+   unordered list (list): the list the be sorted.<br/>
  <li>+/- ordered list (list): the sorted list.
  </ul>

  <p>Examples:<ul>

  <li> <code>.sort([c,a,b],X)</code>: <code>X</code> unifies with
  <code>[a,b,c]</code>.

  <li>
  <code>.sort([C,b(4),A,4,b(1,1),"x",[],[c],[a],[b,c],[a,b],~a(3),a(e,f),b,a(3),b(3),a(10)[30],a(10)[5],a,a(d,e)],X)</code>:
  <code>X</code> unifies with
  <code>[4,"x",[],[a],[c],[a,b],[b,c],a,b,a(3),a(10)[5],a(10)[30],b(3),b(4),a(d,e),a(e,f),b(1,1),~a(3),A,C]</code>.

  <li>
  <code>.sort([3,2,5],[2,3,5])</code>: true.

  <li>
  <code>.sort([3,2,5],[a,b,c])</code>: false.

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
@Manual(
        literal=".sort(list,result)",
        hint="sorts a list of terms using the \"natural\" order. For different types, the order is: numbers &lt; atoms &lt; structures &lt; lists",
        argsHint= {
                "the list the be sorted",
                "the resulting sorted list"
        },
        argsType= {
                "list",
                "list"
        },
        examples= {
                ".sort([C,b(4),A,4,b(1,1),\"x\",[],[c]],X): X unifies with [4,\"x\",[],[c],b(4),b(1,1),A,C]",
                ".sort([a],[b,c],[a,b],~a(3),a(e,f),b,a(3)],X): X unifies with [[a],[a,b],[b,c],b,a(3),a(e,f),~a(3)]",
                ".sort(b(3),a(10)[30],a(10)[5],a,a(d,e)],X): X unifies with [a,a(10)[5],a(10)[30],b(3),a(d,e)]",
                ".sort([3,2,5],[2,3,5]): true",
                ".sort([3,2,5],[a,b,c]): false"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.length",
                "jason.stdlib.member",
                "jason.stdlib.shuffle",
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
public class sort extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new sort();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isList())
            throw JasonException.createWrongArgument(this,"first argument must be a list");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        List<Term> l = ((ListTerm) args[0]).getAsList();
        Collections.sort(l);
        return un.unifies(ASSyntax.createList(l), args[1]);
    }
}
