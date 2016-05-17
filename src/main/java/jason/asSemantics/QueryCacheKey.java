package jason.asSemantics;

import jason.asSyntax.Literal;

public class QueryCacheKey {
    final Literal l;
    //final Unifier u;

    public QueryCacheKey(Literal o1) {
        this.l = o1;
        //this.u = o2;
    }

    @Override
    public int hashCode() {
        return l.getPredicateIndicator().hashCode();
    }
    
    public Literal getLiteral() {
        return l;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof QueryCacheKey) {
            if (obj.hashCode() != this.hashCode()) return false;
            QueryCacheKey o = (QueryCacheKey)obj;
            //System.out.println("* try cache "+o.l+" for "+this.l + " :: "+o.l.compareTo(this.l));
            return new Unifier().unifies(this.l, o.l) && o.l.subsumes(this.l); // the compare is here to avoid using p(10,Y) as cache for p(X,Y). 
            /*
            if (!o.l.getPredicateIndicator().equals(this.l.getPredicateIndicator())) return false; // compares functor & arity
            
            // compare only terms ignoring unbound vars
            for (int i=0; i<l.getArity(); i++) {
                Term t1 = l.getTerm(i);
                Term t2 = o.l.getTerm(i);
                
                if (t2.isVar()) {
                    if (o.u == null) {
                        continue;
                    }
                    t2 = o.u.get((VarTerm)t2); 
                    if (t2 == null)
                        continue; // do not compare
                }
                if (!t1.equals(t2)) // TODO: it is not equals here, should be unification!
                    return false;
            }
            return true;
            */
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "<"+l+">";
    }
}
