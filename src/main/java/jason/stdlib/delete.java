//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

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

  <p>Examples:<ul>
  <li> <code>.delete(a,[a,b,c,a],L)</code>: <code>L</code> unifies with [b,c].
  <li> <code>.delete(a,[a,b,c,a],[c])</code>: false.
  <li> <code>.delete(0,[a,b,c,a],L)</code>: <code>L</code> unifies with [b,c,a].
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

    @Override public int getMinArgs() { return 3; }
    @Override public int getMaxArgs() { return 3; }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        if (args[0].isNumeric() && args[1].isString()) {
            return un.unifies(args[2], deleteFromString((int)((NumberTerm)args[0]).solve(),(StringTerm)args[1]));
        } 
        if (args[0].isNumeric() && args[1].isList()) {
            return un.unifies(args[2], deleteFromList((int)((NumberTerm)args[0]).solve(),(ListTerm)args[1]));
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
            boolean u = un.unifies(element, t); 
            if (u)
                un = bak.clone();
            else
                last = last.append(t.clone());
        }
        return r;
    }

    ListTerm deleteFromList(int index, ListTerm l) {
        ListTerm r = new ListTermImpl();
        ListTerm last = r;
        int i = 0;
        for (Term t: l) {
            if ((i++) != index)
                last = last.append(t.clone());
        }
        return r;
    }
    
    StringTerm deleteFromString(int index, StringTerm st) {
        try {
            String s = st.getString();
            s = s.substring(0,index) + s.substring(index+1);
            return new StringTermImpl(s);
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
