package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.List;

/**

  <p>Internal action: <b><code>.prefix(<i>P</i>,<i>L</i>)</code></b>.
  
  <p>Description: checks if some list <i>P</i> is a prefix of list <i>L</i>. If
  <i>P</i> has free variables, this internal action backtracks all
  possible values for <i>P</i>.

  <p>Parameters:<ul>
  
  <li>+/- prefix (list): the prefix to be checked.</li>
  <li>+ list (list): the list where the prefix is from.</li>
  
  </ul>

  <p>Examples:<ul> 

  <li> <code>.prefix([a],[a,b,c])</code>: true.</li>
  <li> <code>.prefix([b,c],[a,b,c])</code>: false.</li>
  <li> <code>.prefix(X,[a,b,c])</code>: unifies X with any prefix of the list, i.e., [a,b,c], [a,b], [a], and [] in this order;
                                        note that this is different from what its usual implementation in logic programming would result,
                                        where the various prefixes are returned in increasing lengths instead.</li>

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
public class prefix extends DefaultInternalAction {
    
    private static final long serialVersionUID = -4736810884249871078L;
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new prefix();
        return singleton;
    }

    // Needs exactly 2 arguments
    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; } 

    // improve the check of the arguments to also check the type of the arguments
    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isList() && !args[0].isVar())
            throw JasonException.createWrongArgument(this,"first argument must be a list or a variable");
        if (!args[1].isList()) 
            throw JasonException.createWrongArgument(this,"second argument must be a list");
    }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, Term[] args) throws Exception {

        checkArguments(args);

        // execute the internal action

        final Term sublist = args[0];
        final List<Term> list = ((ListTerm)args[1]).getAsList(); // use a Java List for better performance in remove last
        
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
                while (!list.isEmpty()) {
                    ListTerm candidate = ASSyntax.createList(list);
                    list.remove(list.size()-1);
                    c = un.clone();
                    if (c.unifiesNoUndo(sublist, candidate)) {
                        return; // found another sublist, c is the current response
                    }
                }
                if (!triedEmpty) {
                    triedEmpty = true;
                    c = un.clone();
                    if (c.unifiesNoUndo(sublist, ASSyntax.createList())) {
                        return; // found another sublist, c is the current response
                    }                   
                }
                c = null; // no more sublists found 
            }

            public void remove() {}
        };
    }
}
