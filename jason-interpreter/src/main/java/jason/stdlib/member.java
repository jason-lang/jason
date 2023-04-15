package jason.stdlib;

import java.util.Collection;
import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.SetTerm;
import jason.asSyntax.Term;

/**

  <p>Internal action: <b><code>.member(<i>T</i>,<i>L</i>)</code></b>.

  <p>Description: checks if some term <i>T</i> is in a list <i>L</i>. If
  <i>T</i> is a free variable, this internal action backtracks all
  possible values for <i>T</i>.

  <p>Parameters:<ul>

  <li>+/- member (term): the term to be checked.</li>
  <li>+ list (list): the list where the term should be in.</li>

  </ul>

  <p>Examples:<ul>

  <li> <code>.member(c,[a,b,c])</code>: true.</li>
  <li> <code>.member(3,[a,b,c])</code>: false.</li>
  <li> <code>.member(X,[a,b,c])</code>: unifies X with any member of the list.</li>

  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.length
  @see jason.stdlib.sort
  @see jason.stdlib.nth
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse

  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union

*/
@Manual(
        literal=".member(item,list)",
        hint="checks if a given term is in a list. If the term is a free variable, backtracks all possible values",
        argsHint= {
                "the term to be checked",
                "the list where the term should be in"
        },
        argsType= {
                "term",
                "list"
        },
        examples= {
                ".member(c,[a,b,c]): true",
                ".member(3,[a,b,c]): false",
                ".member(X,[a,b,c]): unifies X with any member of the list"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.length",
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
public class member extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new member();
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
        if (!args[1].isList() && !args[1].isSet()) {
            if (args[1] instanceof ObjectTerm) {
                ObjectTerm o = (ObjectTerm)args[1];
                if (o.getObject() instanceof Collection) {
                    return;
                }
            }
            throw JasonException.createWrongArgument(this,"second argument must be a list or a set");
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public Object execute(TransitionSystem ts, final Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        final Term member = args[0];
        final Iterator<Term> i;
        if (args[1].isList()) {
            i = ((ListTerm)args[1]).iterator();
        } else if (args[1].isSet()) {
            if (args[0].isGround()) // use contains
                return ((SetTerm)args[1]).contains(args[0]); // fast track for sets
            i = ((SetTerm)args[1]).iterator();
        } else if (args[1] instanceof ObjectTerm) { // case of queue
            ObjectTerm o = (ObjectTerm)args[1];
            if (o.getObject() instanceof Collection) {
                if (args[0].isGround())
                    return ((Collection<Term>)o.getObject()).contains(args[0]);
                i = ((Collection<Term>)o.getObject()).iterator();
            } else {
                i = ListTerm.EMPTY_LIST.iterator();
            }
        } else {
            i = ListTerm.EMPTY_LIST.iterator();
        }

        return new Iterator<Unifier>() {
            Unifier c = null; // the current response (which is an unifier)

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
                while (i.hasNext()) {
                    c = un.clone();
                    if (c.unifiesNoUndo(member, i.next()))
                        return; // member found in the list, c is the current response
                }
                c = null; // no member is found,
            }

            public void remove() {}
        };

        /* -- old version of the implementation
         * -- problem: even if the user wants only the first member, if search all
        List<Unifier> answers = new ArrayList<Unifier>();
        Unifier newUn = (Unifier)un.clone(); // clone un so as not to change it
        for (Term t: lt) {
            if (newUn.unifies(member, t)) {
                // add this unification to the  answers
                answers.add(newUn);
                newUn = (Unifier)un.clone(); // creates a new clone of un
            }
        }
        return answers.iterator();
        */
    }
}
