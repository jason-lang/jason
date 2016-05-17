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

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
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
    
    @Override public int getMinArgs() { return 3; }
    @Override public int getMaxArgs() { return 3; }

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        return un.unifies(addAnnotToList(args[0], args[1]),args[2]);
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
            Literal result;
            if (l.isAtom()) {
                result = new LiteralImpl((Atom)l);
            } else {
                result = (Literal)l.clone();                
            }
             
            // create the source annots
            //Literal ts = new Pred("source",1).addTerms(source).addAnnots(result.getAnnots("source"));
            Literal ts = Pred.createSource(source).addAnnots(result.getAnnots("source"));
            
            result.delSources();
            result.addAnnot(ts);
            return result;
        } else {
            return l;
        }
    }   
}
