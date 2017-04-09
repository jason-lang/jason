package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.add_annot</code></b>.

  <p>Description: adds an annotation to a literal.

  <p>Parameters:<ul>

  <li>+ belief(s) (literal or list): the literal where the annotation
  is to be added. If this parameter is a list, all literals in the list
  will have the annotation added.<br/>

  <li>+ annotation (structure): the annotation.<br/>

  <li>+/- annotated beliefs(s) (literal or list): this argument
  unifies with the result of the annotation addition.<br/>

  </ul>

  <p>Examples:<ul>

  <li> <code>.add_annot(a,source(jomi),B)</code>: <code>B</code>
  unifies with <code>a[source(jomi)]</code>.</li>

  <li> <code>.add_annot(a,source(jomi),b[jomi])</code>: fails because
  the result of the addition does not unify with the third argument.</li>

  <li> <code>.add_annot([a1,a2], source(jomi), B)</code>: <code>B</code>
  unifies with <code>[a1[source(jomi)], a2[source(jomi)]]</code>.</li>

  </ul>

  <p><b>Note</b>: instead of using this internal action, you can use
  direct unification. <br> <code>.add_annot(a,source(jomi),B)</code> can
  be replaced by <code>B = a[source(jomi)]</code>; <br>
  <code>.add_annot(X,source(jomi),B)</code> can be replaced by <code>B =
  X[source(jomi)]</code>.

  @see jason.stdlib.add_nested_source

 */
public class add_annot extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new add_annot();
        return singleton;
    }

    @Override public int getMinArgs() {
        return 3;
    }
    @Override public int getMaxArgs() {
        return 3;
    }

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        Term result = addAnnotToList(un, args[0], args[1]);
        return un.unifies(result,args[2]);
    }

    protected Term addAnnotToList(Unifier unif, Term l, Term annot) throws JasonException {
        if (l.isList()) {
            ListTerm result = new ListTermImpl();
            for (Term lTerm: (ListTerm)l) {
                Term t = addAnnotToList( unif, lTerm, annot);
                if (t != null) {
                    result.add(t);
                }
            }
            return result;
        } else if (l.isLiteral()) {
            return ((Literal)l).forceFullLiteralImpl().copy().addAnnots(annot);
        }
        return l;
    }
}
