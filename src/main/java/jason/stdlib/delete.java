package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.delete</code></b>.

  <p>Description: delete elements of strings or lists.

  <p>Parameters:<ul>
  <li>+ arg[0] (term, string, or number): if term, this arg is the element to be removed in the list (all occurrences of the element will be removed);
                                          if string, this arg is the substring to be removed (the second arg should be a string);
                                          if number, this arg is the position in the list/string of the element/character to be removed.<br/>
  <li>+ arg[1] (list or string): the list/string where to delete.
  <li>+/- arg[2] (list or string): the list/string with the result of the deletion.
  </ul>
  or
  <li>+ arg[0] (number): where to start to delete
  <li>+ arg[1] (number): where to stop to delete
  <li>+ arg[2] (list or string): the list/string where to delete.
  <li>+/- arg[3] (list or string): the list/string with the result of the deletion.
  </ul>
  
  
  <p>Examples:<ul>
  <li> <code>.delete(a,[a,b,c,a],L)</code>: <code>L</code> unifies with [b,c].
  <li> <code>.delete(a,[a,b,c,a],[c])</code>: false.
  <li> <code>.delete(0,[a,b,c,a],L)</code>: <code>L</code> unifies with [b,c,a].
  <li> <code>.delete(1,3,[a,b,c,a],L)</code>: <code>L</code> unifies with [a,a].
  <li> <code>.delete("a","banana",S)</code>: <code>S</code> unifies with "bnn".
  <li> <code>.delete(0,"banana",S)</code>: <code>S</code> unifies with "anana".
  </ul>

  @see jason.stdlib.concat
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
  @see jason.stdlib.union
*/
public class delete extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new delete();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 4;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        if (args[0].isNumeric()) {
            int nextArg = 1;
            int start = (int)((NumberTerm)args[0]).solve();
            int end   = start+1;
            if (args.length == 4 && args[1].isNumeric()) {
                nextArg = 2;
                end = (int)((NumberTerm)args[1]).solve();
            }
            if (args[nextArg].isString()) {
                return un.unifies(args[nextArg+1], deleteFromString(start,end,(StringTerm)args[nextArg]));
            } else if (args[nextArg].isList()) {
                return un.unifies(args[nextArg+1], deleteFromList(start,end,(ListTerm)args[nextArg]));
            }
        }
        if (args[0].isString() && args[1].isString()) {
            return un.unifies(args[2], deleteFromString((StringTerm)args[0],(StringTerm)args[1]));
        }
        if (args[0].isString()) { // consider arg[1] as string
            return un.unifies(args[2], deleteFromString((StringTerm)args[0], new StringTermImpl(args[1].toString())));
        }


        // first element as term
        if (args[1].isList()) {
            return un.unifies(args[2], deleteFromList(args[0],(ListTerm)args[1], un.clone()));
        }
        throw new JasonException("Incorrect use of the internal action '.delete' (see documentation).");
    }

    ListTerm deleteFromList(Term element, ListTerm l, Unifier un) {
        Unifier bak = un;
        ListTerm r = new ListTermImpl();
        ListTerm last = r;
        for (Term t: l) {
            if (un.unifies(element, t))
                un = bak.clone();
            else
                last = last.append(t.clone());
        }
        return r;
    }

    ListTerm deleteFromList(int index, int end, ListTerm l) {
        ListTerm r = new ListTermImpl();
        ListTerm last = r;
        int i = 0;
        for (Term t: l) {
            if (i < index || i >= end)
                last = last.append(t.clone());
            i++;
        }
        return r;
    }

    StringTerm deleteFromString(int index, int end, StringTerm st) {
        try {
            String s = st.getString();
            return new StringTermImpl( s.substring(0,index) + s.substring(end) );
        } catch (StringIndexOutOfBoundsException e) {
            return st;
        }
    }

    StringTerm deleteFromString(StringTerm st1, StringTerm st2) {
        try {
            String s1 = st1.getString();
            String s2 = st2.getString();
            return new StringTermImpl(s2.replaceAll(s1, ""));
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println(e);
            return st1;
        }
    }
}
