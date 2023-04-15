package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.concat</code></b>.

  <p>Description: concatenates strings or lists.

  <p>Parameters:<ul>
  <li>+ arg[0] ... + arg[n-1] (any term or list): the terms to be concatenated.<br/>
  <li>+/- arg[n]: the result of the concatenation.
  </ul>
  Parameters that are not string are concatenated using the toString method of
  their class.

  <p>Examples:<ul>
  <li> <code>.concat("a","b",X)</code>: <code>X</code> unifies with "ab".
  <li> <code>.concat("a","b","a")</code>: false.
  <li> <code>.concat("a b",1,a,X)</code>: <code>X</code> unifies with "a b1a".
  <li> <code>.concat("a", "b", "c", "d", X)</code>: <code>X</code> unifies with "abcd".
  <li> <code>.concat([a,b,c],[d,e],[f,g],X)</code>: <code>X</code> unifies with <code>[a,b,c,d,e,f,g]</code>.
  </ul>

  <p>Note: this internal action does not implement backtrack. You if need backtrack, you can add
  and use the following rules in your code:
  <pre>
  concat([ ], L, L).
  concat([H|T], L, [H|M]) :- concat(T, L, M).
  </pre>

  @see jason.stdlib.delete
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
@Manual(
        literal=".concat(arg0,arg1[,...],result)",
        hint="concatenates strings or lists",
        argsHint= {
                "the first term",
                "the term to be concatenated with prior",
                "the term to be concatenated with prior ones [optional]",
                "the result of the concatenation"
        },
        argsType= {
                "term or list",
                "term or list",
                "term or list",
                "string"
        },
        examples= {
                ".concat(\"a\",\"b\",X): X unifies with \"ab\"",
                ".concat(\"a\",\"b\",\"a\"): false",
                ".concat(\"a b\",1,a,X): X unifies with \"a b1a\"",
                ".concat(\"a\", \"b\", \"c\", \"d\", X): X unifies with \"abcd\"",
                ".concat([a,b,c],[d,e],[f,g],X): X unifies with [a,b,c,d,e,f,g]"
        },
        seeAlso= {
                "jason.stdlib.delete",
                "jason.stdlib.length",
                "jason.stdlib.member",
                "jason.stdlib.sort",
                "jason.stdlib.shuffle",
                "jason.stdlib.substring",
                "jason.stdlib.prefix",
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
public class concat extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new concat();
        return singleton;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        if (args[0].isList()) {
            // list concat
            if (!args[args.length-1].isVar() && !args[args.length-1].isList()) {
                throw new JasonException("Last argument of concat '"+args[args.length-1]+"'is not a list nor a variable.");
            }
            ListTerm result = (ListTerm)args[0].clone();
            for (int i=1; i<args.length-1; i++) {
                if (!args[i].isList())
                    throw JasonException.createWrongArgument(this,"arg["+i+"] is not a list");
                result.concat((ListTerm)args[i].clone());
            }
            return un.unifies(result, args[args.length-1]);


        } else { //if (args[0].isString() || args) {
            // string concat
            /*if (!args[args.length-1].isVar() && !args[args.length-1].isString()) {
                throw JasonException.createWrongArgument(this,"Last argument '"+args[args.length-1]+"' is not a string nor a variable.");
            }*/
            StringBuilder sr = new StringBuilder( getPlainValue(args[0]) );
            for (int i=1; i<args.length-1; i++) {
                sr.append( getPlainValue(args[i]) );
            }
            Term lastArg = args[args.length-1];
            if (!lastArg.isString() && !lastArg.isVar())
                lastArg = new StringTermImpl(lastArg.toString());
            return un.unifies(new StringTermImpl(sr.toString()), lastArg);
            //} else {
            //    throw JasonException.createWrongArgument(this,"First argument '"+args[0]+"' must be a list, string or term.");
        }
    }

    String getPlainValue(Term t) {
        if (t.isString()) {
            return ((StringTerm)t).getString();
        }
        String r = t.toString();
        if (t.isAtom() && r.startsWith("'") && r.endsWith("'"))
            return r.substring(1, r.length()-1);
        return r;
    }
}
