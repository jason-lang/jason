
package jason.stdlib;

import java.util.Iterator;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.substring</code></b>.

  <p>Description: checks if a string is sub-string of another
    string. The arguments can be other kinds of terms, in which case
    the toString() of the term is used. If "position" is a
    free variable, the internal action backtracks all possible values
    for the positions where the sub-string occurs in the string.

  <p>Parameters:<ul>
  <li>+ substring (any term).<br/>
  <li>+/- string (any term).<br/>
  <li>+/- start position (optional -- integer): the initial position of
  the string where the sub-string occurs.
  <li>+/- end position (optional -- integer): the position in the string where the sub-string ends.
  </ul>

  <p>Examples:<ul>
  <li> <code>.substring("b","aaa")</code>: false.
  <li> <code>.substring("b","aaa",X)</code>: false.
  <li> <code>.substring("a","bbacc")</code>: true.
  <li> <code>.substring("a","abbacca",X)</code>: true and <code>X</code> unifies with 0, 3, and 6.
  <li> <code>.substring("a","bbacc",0)</code>: false. When the third argument is 0, .substring works like a java <b>startsWith</b> method.
  <li> <code>.substring(a(10),b(t1,a(10)),X)</code>: true and <code>X</code> unifies with 5.
  <li> <code>.substring(a(10),b("t1,a(10),kk"),X)</code>: true and <code>X</code> unifies with 6.
  <li> <code>.substring(R,a(10,20),5)</code>: true and <code>R</code> unifies with "20)".
  <li> <code>.substring(R,a(10,20),5,7)</code>: true and <code>R</code> unifies with "20".
  </ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.reverse

*/
@SuppressWarnings("serial")
public class substring extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new substring();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 2;
    }
    @Override public int getMaxArgs() {
        return 4;
    }

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
        } else if (args[2].isGround() && args[2].isNumeric() && args[0].isVar()) {
            // case of .substring(X,"a(10,20)",5,7)
            // no backtracking utilisation
            // unifies the var with the substring
            int start = (int)((NumberTerm)(args[2])).solve();
            int end   = s1.length();
            if (args.length == 4 && args[3].isNumeric())
                end = (int)((NumberTerm)(args[3])).solve();
            return un.unifies(args[0], new StringTermImpl( s1.substring(start,end)));
        } else if (args[2].isGround() && args[2].isNumeric() && args[1].isVar()) {
            // case of .substring("a(10,20)",X,5,7) ==> which is wrong X ("20") is not substring of "a(10,20)"
            // however this option is kept for compatibility reasons
            int start = (int)((NumberTerm)(args[2])).solve();
            int end   = s0.length();
            if (args.length == 4 && args[3].isNumeric())
                end = (int)((NumberTerm)(args[3])).solve();
            return un.unifies(args[1], new StringTermImpl( s0.substring(start,end)));
        } else {

            // backtrack version: unifies in the third argument all possible positions of s0 in s1
            return new Iterator<Unifier>() {
                Unifier c = null; // the current response (which is an unifier)
                int     pos = 0;  // current position in s1

                { find(); }

                public boolean hasNext() {
                    return c != null;
                }

                public Unifier next() {
                    Unifier b = c;
                    find(); // find next response
                    return b;
                }

                void find() {
                    while (pos < s1.length()) {
                        pos = s1.indexOf(s0,pos);
                        if (pos < 0) {
                            // quit without solution
                            pos = s1.length();
                        } else {
                            c = (Unifier)un.clone();
                            if (c.unifiesNoUndo(args[2], new NumberTermImpl(pos++))) {
                                if (args.length == 4) {
                                    if (c.unifiesNoUndo(args[3], new NumberTermImpl( (pos-2)+s0.length() ))) {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                    c = null; // no member is found,
                }

                public void remove() {}
            };
            //return un.unifies(args[2], new NumberTermImpl(pos));
        }
    }
}

