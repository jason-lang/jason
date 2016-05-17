package jason.asSyntax;

import jason.NoValueException;
import jason.asSemantics.Agent;
import jason.asSemantics.ArithFunction;
import jason.asSemantics.Unifier;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an arithmetic function, like math.max(arg1,arg2) -- a functor (math.max) and two arguments.
 * A Structure is thus used to store the data.
 * 
 * @composed - "arguments (from Structure.terms)" 0..* Term
 * 
 * @author Jomi
 *
 */
public class ArithFunctionTerm extends Structure implements NumberTerm {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ArithFunctionTerm.class.getName());
    
    protected NumberTerm value = null; // value, when evaluated

    private ArithFunction function = null;
    
    private Agent agent = null; // the agent where this function was used

    public ArithFunctionTerm(ArithFunction function) {
        super(function.getName(), 2);
        this.function = function;   
    }

    public ArithFunctionTerm(ArithFunctionTerm af) {
        super(af); // clone args from af
        function = af.function;
        agent    = af.agent;
    }
    
    public ArithFunctionTerm(String functor, int arity) {
        super(functor,arity);
    }
        
    @Override
    public boolean isNumeric() {
        return true;
    }
    
    @Override
    public boolean isAtom() {
        return false;
    }
    
    @Override
    public boolean isStructure() {
        return false;
    }
    
    @Override
    public boolean isLiteral() {
        return false;
    }

    @Override
    public boolean isArithExpr() {
        return true;
    }
    
    public void setAgent(Agent ag) {
        agent = ag;
    }
    public Agent getAgent() {
        return agent;
    }
    
    /** computes the value for this arithmetic function (as defined in the NumberTerm interface) */
    @Override
    public Term capply(Unifier u) {
        if (function == null) {
            logger.log(Level.SEVERE, getErrorMsg()+ " -- the function can not be evalutated, it has no function assigned to it!", new Exception());
        } else {
            Term v = super.capply(u);
            if (function.allowUngroundTerms() || v.isGround()) {
                try {         
                    value = new NumberTermImpl(function.evaluate((agent == null ? null : agent.getTS()), ((Literal)v).getTermsArray()));
                    return value;
                } catch (NoValueException e) {
                    // ignore and return this;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, getErrorMsg()+ " -- error in evaluate!", e);
                }
            //} else {
            //    logger.warning(getErrorMsg()+ " -- this function has unground arguments and can not be evaluated! Unifier is "+u);  
            }
        }
        return clone();
    }

    public double solve() throws NoValueException {
        if (value == null) // try to solve without unifier
            capply(null);
        if (value == null)
            throw new NoValueException();
        else
            return value.solve();
    }
    
    public boolean checkArity(int a) {
        return function != null && function.checkArity(a);
    }
    
    @Override
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un)  {
        logger.log(Level.WARNING, "Arithmetic term cannot be used for logical consequence!", new Exception());
        return LogExpr.EMPTY_UNIF_LIST.iterator();
    }
    
    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        return super.equals(t);
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof VarTerm) {
            return o.compareTo(this) * -1;
        }
        return super.compareTo(o);
        /*if (o instanceof NumberTerm) {
            NumberTerm st = (NumberTerm)o;
            if (solve() > st.solve()) return 1;
            if (solve() < st.solve()) return -1;
            return 0;
        }
        return -1;*/
    }

    @Override    
    public String getErrorMsg() {
        return "Error in '"+this+"' ("+ super.getErrorMsg() + ")";       
    }

    @Override
    public NumberTerm clone() {
        return new ArithFunctionTerm(this);
    } 
    
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("expression");
        u.setAttribute("type", "arithmetic");
        Element r = (Element) document.createElement("right");
        r.appendChild(super.getAsDOM(document)); // put the left argument indeed!
        u.appendChild(r);
        return u;
    }
}
