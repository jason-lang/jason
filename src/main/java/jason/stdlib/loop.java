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
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.PlanBody.BodyType;

import java.util.Iterator;

/** 
Implementation of <b>while</b>. 

<p>Syntax:
<pre>
  while ( <i>logical formula</i> ) {
     <i>plan_body</i>
  }
</pre>
</p>

<p>while <i>logical formula</i> holds, the <i>plan_body</i> is executed.</p>

<p>Example:
<pre>
+event : context
  <- ....
     while(vl(X) & X > 10) { // where vl(X) is a belief
       .print("value > 10");
       -+vl(X+1);
     }
     ....
</pre>
The unification resulted from the evaluation of the logical formula is used only inside the loop,
i.e., the unification after the while is the same as before.
</p>

@see jason.stdlib.foreach for

*/
public class loop extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new loop();
        return singleton;
    }
    
    @Override public Term[] prepareArguments(Literal body, Unifier un) {
        /*Term[] terms = new Term[body.getArity()];
        for (int i=0; i<terms.length; i++) {
            terms[i] = body.getTerm(i).clone();
        }
        return terms;*/
        return body.getTermsArray();
    }
        
    @Override public int getMinArgs() { return 2; }
    @Override public int getMaxArgs() { return 2; }
    
    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if ( !(args[0] instanceof LogicalFormula))
            throw JasonException.createWrongArgument(this,"first argument (test) must be a logical formula.");
        if ( !args[1].isPlanBody())
            throw JasonException.createWrongArgument(this,"second argument must be a plan body term.");
    }
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
            
        IntendedMeans im = ts.getC().getSelectedIntention().peek();
        PlanBody whileia = im.getCurrentStep();

        // if the IA has a backup unifier, use that (it is an object term)
        if (args.length == 2) {
            // first execution of while
            checkArguments(args);
            // add backup unifier in the IA
            whileia = new PlanBodyImpl(BodyType.internalAction, whileia.getBodyTerm().clone());
            whileia.add(im.getCurrentStep().getBodyNext());
            ((Structure)whileia.getBodyTerm()).addTerm(new ObjectTermImpl(un.clone()));
        } else if (args.length == 3) {            
            // restore the unifier of previous iterations
            Unifier ubak = (Unifier)((ObjectTerm)args[2]).getObject();
            un.clear();
            un.compose(ubak);
        } else {
            throw JasonException.createWrongArgumentNb(this);
        }
        
        LogicalFormula logExpr = (LogicalFormula)args[0]; 
        // perform one iteration of the loop
        Iterator<Unifier> iu = logExpr.logicalConsequence(ts.getAg(), un); 
        if (iu.hasNext()) { 
            un.compose(iu.next());
            
            // add in the current intention:
            // 1. the body argument and
            // 2. the while internal action after the execution of the body
            //    (to test the loop again)
            PlanBody whattoadd = (PlanBody)args[1].clone();
            whattoadd.add(whileia); // the add clones whileia
            whattoadd.setAsBodyTerm(false);
            im.insertAsNextStep(whattoadd);
        }
        return true;
    }
}
