package jason.stdlib;

import java.util.Iterator;
import java.util.List;

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

  <p>Internal action: <b><code>.prefix(<i>P</i>,<i>L</i>)</code></b>.

  <p>Description: checks if some list/string <i>P</i> is a prefix of list/string<i>L</i>. If
  <i>P</i> has free variables, this internal action backtracks all
  possible values for <i>P</i>.

  <p>Parameters:<ul>

  <li>+/- prefix (list or string): the prefix to be checked.</li>
  <li>+ list (list or string): the list where the prefix is from.</li>

  </ul>

  <p>Examples:<ul>

  <li> <code>.prefix([a],[a,b,c])</code>: true.</li>
  <li> <code>.prefix([a,b],[a,b,c])</code>: true.</li>
  <li> <code>.prefix([b,c],[a,b,c])</code>: false.</li>
  <li> <code>.prefix(X,[a,b,c])</code>: unifies X with any prefix of the list, i.e., [a,b,c], [a,b], [a], and [] in this order;
                                        note that this is different from what its usual implementation in logic programming would result,
                                        where the various prefixes are returned in increasing lengths instead.</li>

  <li> <code>.prefix("a","abc")</code>: true.</li>
  <li> <code>.prefix("bc","abc")</code>: false.</li>
  <li> <code>.prefix(X,"abc")</code>: unifies X with any prefix of the string, i.e., "abc", "ab", "a", and "" in this order.
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.length
  @see jason.stdlib.sort
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse
  @see jason.stdlib.suffix
  @see jason.stdlib.sublist

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
@Manual(
        literal=".prefix(prefix,list)",
        hint="checks if some list is a prefix of other list, backtracking all free variables",
        argsHint= {
                "the prefix to be checked",
                "the list where the prefix is from"
        },
        argsType= {
                "list",
                "list"
        },
        examples= {
                ".prefix([a],[a,b,c]): true",
                ".prefix([a,b],[a,b,c]): true",
                ".prefix([b,c],[a,b,c]): false",
                ".prefix(X,[a,b,c]): unifies X with any prefix of the list, i.e., [a,b,c], [a,b], [a], and [] in this order"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.length",
                "jason.stdlib.sublist",
                "jason.stdlib.sort",
                "jason.stdlib.shuffle",
                "jason.stdlib.suffix",
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
public class prefix extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new prefix();
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
        final List<Term> list;
        final StringBuilder string;
        if (isListCase) {
            list = ((ListTerm)args[1]).getAsList(); // use a Java List for better performance in remove last
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
                if (c == null) find();
                Unifier b = c;
                find(); // find next response
                return b;
            }

            void find() {
                if (isListCase) {
                    while (!list.isEmpty()) {
                        Term candidate = ASSyntax.createList(list);
                        list.remove(list.size()-1);
                        c = un.clone();
                        if (c.unifiesNoUndo(sublist, candidate)) {
                            return; // found another sublist, c is the current response
                        }
                    }
                } else {
                    while (string.length() != 0) {
                        Term candidate = ASSyntax.createString(string.toString());
                        string.deleteCharAt(string.length()-1);
                        c = un.clone();
                        if (c.unifiesNoUndo(sublist, candidate)) {
                            return; // found another sublist, c is the current response
                        }
                    }
                }
                if (!triedEmpty) {
                    triedEmpty = true;
                    c = un.clone();
                    if (c.unifiesNoUndo(sublist, (isListCase ? ASSyntax.createList() : ASSyntax.createString("") ) )) {
                        return; // found another sublist, c is the current response
                    }
                }
                c = null; // no more sublists found
            }

            public void remove() {}
        };
    }
}
