package jason.asSyntax;

import jason.asSemantics.Unifier;
import jason.util.ToDOM;

import java.io.Serializable;
import java.util.Map;

/**
 * Common interface for all kind of terms
 * 
 * @opt nodefillcolor lightgoldenrodyellow
 */
public interface Term extends Cloneable, Comparable<Term>, Serializable, ToDOM {

    public boolean isVar();
    public boolean isUnnamedVar();
    public boolean isLiteral();
    public boolean isRule();
    public boolean isList();
    public boolean isString();
    public boolean isInternalAction();
    public boolean isArithExpr();
    public boolean isNumeric();
    public boolean isPred();
    public boolean isGround();
    public boolean isStructure();
    public boolean isAtom();
    public boolean isPlanBody();
    public boolean isCyclicTerm();

    public boolean hasVar(VarTerm t, Unifier u);
    public VarTerm getCyclicVar();
    
    public void countVars(Map<VarTerm, Integer> c);

    public Term clone();

    public boolean equals(Object o);
    
    public boolean subsumes(Term l); 

    /** replaces variables by their values in the unifier, returns true if some variable was applied */
    //public boolean apply(Unifier u);

    /** clone and applies together (and faster than clone and then apply) */
    public Term capply(Unifier u);
    
    /** Removes the value cached for hashCode */
    //public void resetHashCodeCache();

    public void setSrcInfo(SourceInfo s);
    public SourceInfo getSrcInfo();
}
