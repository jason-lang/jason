package jason.stdlib;

import java.util.Iterator;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
<p>Internal action: <b><code>.nth</code></b>.

<p>Description: gets the nth term of a list.

<p>Parameters:<ul>
<li>-/+ index (integer): the position of the term (the first term is at position 0)<br/>
<li>+ list (list or string): the list/string where to get the term from.<br/>
<li>-/+ term (term): the term at position <i>index</i> in the <i>list</i>.<br/>
</ul>

<p>Examples:<ul>
<li> <code>.nth(0,[a,b,c],X)</code>: unifies <code>X</code> with <code>a</code>.
<li> <code>.nth(2,[a,b,c],X)</code>: unifies <code>X</code> with <code>c</code>.
<li> <code>.nth(2,"abc",X)</code>: unifies <code>X</code> with <code>c</code>.
<li> <code>.nth(0,[a,b,c],d)</code>: false.
<li> <code>.nth(0,[a,b,c],a)</code>: true.
<li> <code>.nth(5,[a,b,c],X)</code>: error.
<li> <code>.nth(X,[a,b,c,a,e],a)</code>: unifies <code>X</code> with <code>0</code> (and <code>3</code> if it backtracks).
</ul>

  @see jason.stdlib.concat
  @see jason.stdlib.delete
  @see jason.stdlib.length
  @see jason.stdlib.member
  @see jason.stdlib.sort
  @see jason.stdlib.max
  @see jason.stdlib.min
  @see jason.stdlib.reverse


  @see jason.stdlib.difference
  @see jason.stdlib.intersection
  @see jason.stdlib.union
*/
@Manual(
        literal=".nth(index,list,term)",
        hint="gets the nth term of a list",
        argsHint= {
                "the position of the term (the first term is at position 0)",
                "the list where to get the term from",
                "the term at referred position of the list."
        },
        argsType= {
                "integer",
                "list",
                "term"
        },
        examples= {
                ".nth(0,[a,b,c],X): unifies X with a",
                ".nth(2,[a,b,c],X): unifies X with c",
                ".nth(0,[a,b,c],d): false",
                ".nth(0,[a,b,c],a): true",
                ".nth(5,[a,b,c],X): error",
                ".nth(X,[a,b,c,a,e],a): unifies X with 0 (and 3 if it backtracks)"
        },
        seeAlso= {
                "jason.stdlib.concat",
                "jason.stdlib.delete",
                "jason.stdlib.length",
                "jason.stdlib.member",
                "jason.stdlib.sort",
                "jason.stdlib.max",
                "jason.stdlib.min",
                "jason.stdlib.reverse",
                "jason.stdlib.difference",
                "jason.stdlib.intersection",
                "jason.stdlib.union"
        }
    )
@SuppressWarnings("serial")
public class nth extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new nth();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isNumeric() && !args[0].isVar()) {
            throw JasonException.createWrongArgument(this,"the first argument should be numeric or a variable -- not '"+args[0]+"'.");
        }
        if (!args[1].isList() && !args[1].isString()) {
            throw JasonException.createWrongArgument(this,"the second argument should be a list or string and not '"+args[1]+"'.");
        }
    }

    @Override
    public Object execute(TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        checkArguments(args);


        ListTerm list = null;
        if (args[1].isList())
            list = (ListTerm)args[1];
        else if (args[1].isString()) {
            list = new ListTermImpl();
            for (byte b: ((StringTerm)args[1]).getString().getBytes()) {
                list.add(new StringTermImpl(new String(new byte[] {b})));
            }
        }

        if (args[0].isNumeric()) {
            int index = (int)((NumberTerm)args[0]).solve();

            if (index < 0 || index >= list.size()) {
                throw new JasonException("nth: index "+index+" is out of bounds ("+list.size()+")");
            }

            return un.unifies(args[2], list.get(index));
        }

        if (args[0].isVar()) {

            final Iterator<Term> ilist = list.iterator();

            // return all indexes for thirds arg
            return new Iterator<Unifier>() {
                int index = -1;
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
                    while (ilist.hasNext()) {
                        index++;
                        Term candidate = ilist.next();
                        c = un.clone();
                        if (c.unifiesNoUndo( args[2], candidate)) {
                            c.unifies(args[0], ASSyntax.createNumber(index));
                            return; // found another response
                        }
                    }
                    c = null; // no more sublists found
                }

                public void remove() {}
            };

        }
        return false;
    }
}
