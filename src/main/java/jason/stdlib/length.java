package jason.stdlib;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
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
        Term l1 = args[0];
        Term l2 = args[1];

        NumberTerm size = null;
        if (l1.isList()) {
            ListTerm lt = (ListTerm) l1;
            size = new NumberTermImpl(lt.size());
        } else if (l1.isString()) {
            StringTerm st = (StringTerm) l1;
            size = new NumberTermImpl(st.getString().length());
        }
        if (size != null) {
            return un.unifies(l2, size);
        }
        return false;
    }
}
