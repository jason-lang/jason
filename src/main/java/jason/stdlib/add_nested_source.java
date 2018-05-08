package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.add_nested_source</code></b>.

  <p>Description: adds a source annotation to a literal (used in communication).

  <p>Parameters:<ul>

  <li>+ belief(s) (literal or list): the literal where the source annotation
  is to be added. If this parameter is a list, all literals in the list
  will have the source added.<br/>

  <li>+ source (atom): the source.<br/>

  <li>+/- annotated beliefs(s) (literal or list): this argument
  unifies with the result of the source addition.<br/>

  </ul>

  <p>Examples:<ul>

  <li> <code>.add_nested_source(a,jomi,B)</code>: <code>B</code>
  unifies with <code>a[source(jomi)]</code>.</li>

  <li> <code>.add_nested_source([a1,a2], jomi, B)</code>: <code>B</code>
  unifies with <code>[a1[source(jomi)], a2[source(jomi)]]</code>.</li>

  <li> <code>.add_nested_source(a[source(bob)],jomi,B)</code>:
  <code>B</code> unifies with <code>a[source(jomi)[source(bob)]]</code>,
  which means `I believe in <code>a</code> and the source for that is
  agent jomi, the source for jomi was bob'; bob sent a tell to jomi that
  sent a tell to me.</li>

  </ul>

 */
public class add_nested_source extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new add_nested_source();
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
        try {
        	return un.unifies(addAnnotToList(args[0], args[1]),args[2]);
        } catch (Exception e) {
			throw new JasonException("Error adding nest source '"+args[1]+"' to "+args[0], e);
		}
    }

    public static Term addAnnotToList(Term l, Term source) {
        if (l.isList()) {
            ListTerm result = new ListTermImpl();
            for (Term lTerm: (ListTerm)l) {
                Term t = addAnnotToList( lTerm, source);
                if (t != null) {
                    result.add(t);
                }
            }
            return result;
        } else if (l.isLiteral()) {
            Literal result = ((Literal)l).forceFullLiteralImpl().copy();

            // create the source annots
            Literal ts = Pred.createSource(source).addAnnots(result.getAnnots("source"));

            result.delSources();
            result.addAnnot(ts);
            return result;
        } else {
            return l;
        }
    }
}
