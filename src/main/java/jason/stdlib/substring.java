
package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.util.Iterator;

/**
  <p>Internal action: <b><code>.substring</code></b>.

  <p>Description: checks if a string is sub-string of another
    string. The arguments can be other kinds of terms, in which case
    the toString() of the term is used. If "position" is a
    free variable, the internal action backtracks all possible values
    for the positions where the sub-string occurs in the string.

  <p>Parameters:<ul>
  <li>+ substring (any term).<br/>
  <li>+ string (any term).<br/>
  <li>+/- position (optional -- integer): the position of
  the string where the sub-string occurs. 
  </ul>

  <p>Examples:<ul>
  <li> <code>.substring("b","aaa")</code>: false.
  <li> <code>.substring("b","aaa",X)</code>: false.
  <li> <code>.substring("a","bbacc")</code>: true.
  <li> <code>.substring("a","abbacca",X)</code>: true and <code>X</code> unifies with 0, 3, and 6.
  <li> <code>.substring("a","bbacc",0)</code>: false.
  <li> <code>.substring(a(10),b(t1,a(10)),X)</code>: true and <code>X</code> unifies with 5.
  <li> <code>.substring(a(10),b("t1,a(10)"),X)</code>: true and <code>X</code> unifies with 6.
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.reverse

*/
public class substring extends DefaultInternalAction {
    
    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new substring();
        return singleton;
    }

    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 3; }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);
        
        final String s0;
        if (args[0].isString())
            s0 = ((StringTerm)args[0]).getString();
        else 
            s0 = args[0].toString();
        
        final String s1;
        if (args[1].isString())
            s1 = ((StringTerm)args[1]).getString();
        else
            s1 = args[1].toString();

        if (args.length == 2) {
            // no backtracking utilisation
            return s1.indexOf(s0) >= 0;
        } else {
            
            // backtrack version: unifies in the third argument all possible positions of s0 in s1
            return new Iterator<Unifier>() {
                Unifier c = null; // the current response (which is an unifier)
                int     pos = 0;  // current position in s1
                
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
                    if (pos < s1.length()) {
                        pos = s1.indexOf(s0,pos);
                        if (pos >= 0) {
                            c = (Unifier)un.clone();
                            c.unifiesNoUndo(args[2], new NumberTermImpl(pos));
                            pos++;
                            return;
                        }
                        pos = s1.length(); // to stop searching
                    }
                    c = null; // no member is found,
                }

                public void remove() {}
            };
            //return un.unifies(args[2], new NumberTermImpl(pos));
        }
    }
}

