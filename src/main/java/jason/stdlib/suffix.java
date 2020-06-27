package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.suffix(<i>S</i>,<i>L</i>)</code></b>.

  <p>Description: checks if some list or string <i>S</i> is a suffix of list/string <i>L</i>. If
  <i>S</i> has free variables, this internal action backtracks all
  possible values for <i>S</i>.

  <p>Parameters:<ul>

  <li>+/- suffix (list or string): the suffix to be checked.</li>
  <li>+ list (list or string): the list where the suffix is from.</li>

  </ul>

  <p>Examples:<ul>

  <li> <code>.suffix([c],[a,b,c])</code>: true.</li>
  <li> <code>.suffix([a,b],[a,b,c])</code>: false.</li>
  <li> <code>.suffix(X,[a,b,c])</code>: unifies X with any suffix of the list, i.e., [a,b,c], [b,c], [c], and [] in this order.</li>

  <li> <code>.suffix("c","abc")</code>: true.</li>
  <li> <code>.suffix("ab","abc")</code>: false.</li>
  <li> <code>.suffix(X,"abc")</code>: unifies X with any suffix of the string, i.e., "abc", "bc", "c", and "" in this order.</li>

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.length
  @see jason.stdlib.sort
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse
  @see jason.stdlib.prefix
  @see jason.stdlib.sublist

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
@Manual(
        literal=".suffix(suffix,list)",
        hint="checks if some list is a suffix of other list, backtracking all free variables",
        argsHint= {
                "the suffix to be checked",
                "the list where the suffix is from"
        },
        argsType= {
                "list",
                "list"
        },
        examples= {
                ".suffix([c],[a,b,c]): true" +
                ".suffix([a,b],[a,b,c]): false" +
                ".suffix(X,[a,b,c]): unifies X with any suffix of the list, i.e., [a,b,c], [b,c], [c], and [] in this order"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.length",
                "jason.stdlib.sort",
                "jason.stdlib.nth",
                "jason.stdlib.max",
                "jason.stdlib.min",
                "jason.stdlib.reverse",
                "jason.stdlib.prefix",
                "jason.stdlib.sublist",
                "jason.stdlib.difference",
                "jason.stdlib.intersection",
                "jason.stdlib.union"
        }
    )
@SuppressWarnings("serial")
public class suffix extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new suffix();
        return singleton;
    }

    // Needs exactly 2 arguments
    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 2;
    }

    // improve the check of the arguments to also check the type of the arguments
    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isList() && !args[0].isVar() && !args[0].isString())
            throw JasonException.createWrongArgument(this,"first argument must be a list, string, or a variable");
        if (!args[1].isList() && !args[1].isString())
            throw JasonException.createWrongArgument(this,"second argument must be either a list or a string");
    }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, Term[] args) throws Exception {

        checkArguments(args);

        // execute the internal action

        final Term sublist = args[0];

        boolean isListCase = args[1].isList();
        final Iterator<ListTerm> list;
        final StringBuilder string;
        if (isListCase) {
            list = ((ListTerm)args[1]).listTermIterator();
            string = null;
        } else {
            list = null;
            string = new StringBuilder(((StringTerm)args[1]).getString());
        }

        return new Iterator<Unifier>() {
            Unifier c = null; // the current response (which is an unifier)
            boolean triedEmpty = false;

            public boolean hasNext() {
                if (c == null) // the first call of hasNext should find the first response
                    find();
                return c != null;
            }

            public Unifier next() {
                if (c == null)
                    find();
                Unifier b = c;
                find(); // find next response
                return b;
            }

            void find() {
                if (isListCase) {
                    while (list.hasNext()) {
                        ListTerm l = list.next();
                        if (l.isVar()) // the case of the tail of the list
                            break;
                        c = un.clone();
                        if (c.unifiesNoUndo(sublist, ASSyntax.createList(l))) {
                            return; // found another sublist, c is the current response
                        }
                    }
                } else {
                    while (string.length() > 0) {
                        Term candidate = ASSyntax.createString(string.toString());
                        string.deleteCharAt(0);
                        c = un.clone();
                        if (c.unifiesNoUndo(sublist, candidate)) {
                            return; // found another sublist, c is the current response
                        }
                    }
                    if (!triedEmpty) {
                        triedEmpty = true;

                        c = un.clone();
                        if (c.unifiesNoUndo(sublist, ASSyntax.createString(""))) {
                            return; // found another sublist, c is the current response
                        }
                    }
                }
                c = null; // no more sublists found
            }

            public void remove() {}
        };
    }
}
