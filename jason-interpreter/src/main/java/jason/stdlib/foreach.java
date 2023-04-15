package jason.stdlib;

import java.util.Iterator;

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
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

/**
  <p>Internal action: <b><code>.for(formula) { plan_body }</code></b>.

  <p>Description: Implementation of <b>for</b>. For all unifications of <i>logical formula</i>,
  the <i>plan_body</i> is executed.

  <p>Syntax:
  <pre>
    for ( <i>logical formula</i> ) {
       <i>plan_body</i>
    }
  </pre>
  </p>

  <p>Parameters:<ul>

  <li>+ formula (logical formula): the formula that when true makes the plan_body be executed.
  <br/>

  </ul>

  <p>Example:
  <pre>
  +event : context
    <- ....
       for ( vl(X) ) {
          .print(X);     // print all values of X
       }
       for ( .member(X,[a,b,c]) ) {
          .print(X);    // print all members of the list
       }
       for ( .range(I,1,10) ) {
          .print(I);    // print all values from 1 to 10
       }
       ....
  </pre>
  The unification resulted from the evaluation of the logical formula is used only inside the loop,
  i.e., the unification after the for is the same as before.
  </p>

@see jason.stdlib.loop while

*/
@Manual(
        literal="for (formula) { plan_body }",
        hint="for all unifications of logical formula, the plan_body is executed",
        argsHint= {
                "the formula that when true makes the plan_body be executed"
        },
        argsType= {
                "logical formula"
        },
        examples= {
                "for ( vl(X) ) { .print(X); }: print all values of X",
                "for ( .member(X,[a,b,c]) ) { .print(X); }: print all members of the list",
                "for ( .range(I,1,10) ) { .print(I); }: print all values from 1 to 10"
        },
        seeAlso= {
                ""
        }
    )
@SuppressWarnings("serial")
public class foreach extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
            if (singleton == null)
                singleton = new foreach();
            return singleton;
        }

        @Override public Term[] prepareArguments(Literal body, Unifier un) {
            /*Term[] terms = new Term[body.getArity()];
            for (int i=0; i<terms.length; i++) {
                terms[i] = body.getTerm(i).clone();
            }
            return terms;
            */
            return body.getTermsArray();
        }

        @Override public int getMinArgs() {
            return 2;
        }
        @Override public int getMaxArgs() {
            return 2;
        }

        @Override protected void checkArguments(Term[] args) throws JasonException {
            super.checkArguments(args); // check number of arguments
            if ( !(args[0] instanceof LogicalFormula))
                throw JasonException.createWrongArgument(this,"first argument must be a logical formula.");
            if ( !args[1].isPlanBody())
                throw JasonException.createWrongArgument(this,"second argument must be a plan body term.");
        }


        @SuppressWarnings("unchecked")
        @Override
        public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
            IntendedMeans im    = ts.getC().getSelectedIntention().peek();
            PlanBody      foria = im.getCurrentStep();

            Iterator<Unifier> iu;

            if (args.length == 2) {
                // first execution of while
                checkArguments(args);

                // get all solutions for the loop
                // Note: we have to get all solutions here, otherwise a concurrent modification will occur for the iterator
                //       it seems not necessary anymore (maybe due to the use of data structures that better support concurrency)
                //       so I commented the lines and get back to the lazy implementation
                LogicalFormula logExpr = (LogicalFormula)args[0];
                iu = logExpr.logicalConsequence(ts.getAg(), un.clone());
                if (!iu.hasNext())
                    return true;
                /*List<Unifier> allsol = new ArrayList<Unifier>();
                while (iu.hasNext())
                    allsol.add(iu.next());
                iu = allsol.iterator();*/
                foria = new PlanBodyImpl(BodyType.internalAction, foria.getBodyTerm().clone());
                foria.add(im.getCurrentStep().getBodyNext());
                Structure forstructure = (Structure)foria.getBodyTerm();
                forstructure.addTerm(new ObjectTermImpl(iu));         // store all solutions
                forstructure.addTerm(new ObjectTermImpl(un.clone())); // backup original unifier
            } else if (args.length == 4) {
                // restore the solutions
                iu = (Iterator<Unifier>)((ObjectTerm)args[2]).getObject();
            } else {
                throw JasonException.createWrongArgumentNb(this);
            }

            un.clear();
            if (iu.hasNext()) {
                // add in the current intention:
                // 1. the body argument of for and
                // 2. the for internal action after the execution of the body
                //    (to perform the next iteration)
                un.compose(iu.next());
                PlanBody whattoadd = (PlanBody)args[1].clone();
                whattoadd.add(foria);
                whattoadd.setAsBodyTerm(false);
                im.insertAsNextStep(whattoadd);
            } else {
                un.compose((Unifier)((ObjectTerm)args[3]).getObject());
            }
            return true;
        }
    }
