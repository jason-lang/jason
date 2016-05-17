package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.Iterator;

/** 

   Wraps a rule into a function. For example the rule
     sum(X,Y,Z) :- Z = X+Y.
   is wrapped in a function sum(X,Y). 
   
   <p>To define sum as
   a function the user should register it using a
   directive in the ASL code:
     { register_function("myf.sum",2,"sum") }
   where myf.sum is the name of the function,
   sum is the name of the rule (or literal, or
   internal action) and 2 is the function's arity.
      
   @author Jomi 
*/
public class RuleToFunction extends DefaultArithFunction  {

    private final String literal;
    private final int    arity;
    
    public RuleToFunction(String literal, int arity) {
        this.literal = literal;
        this.arity = arity;
    }
    
    @Override
    public String getName() {
        return super.getName()+"_{"+literal+"}";
    }
    
    @Override
    public boolean checkArity(int a) {
        return a == arity;
    }
    
    @Override
    public boolean allowUngroundTerms() {
        return true;
    }
    
    @Override
    public double evaluate(TransitionSystem ts, Term[] args) throws Exception {
        // create a literal to perform the query
        Literal r;
        if (literal.indexOf(".") > 0) // is internal action
            r = new InternalActionLiteral(literal);
        else
            r = new LiteralImpl(literal);
        
        r.addTerms(args);
        VarTerm answer = new VarTerm("__RuleToFunctionResult");
        r.addTerm(answer);
        
        // query the BB
        Iterator<Unifier> i = r.logicalConsequence( (ts == null ? null : ts.getAg()), new Unifier());
        if (i.hasNext()) {
            Term value = i.next().get(answer);
            if (value.isNumeric())
                return ((NumberTerm)value).solve();
            else
                throw new JasonException("The result of "+r+" (="+value+") is not numeric!");             
        } else 
            throw new JasonException("No solution was found for rule "+r);
    }
    
    public String toString() { 
        return "function wrapper for "+literal+"/"+arity;
    }
}
