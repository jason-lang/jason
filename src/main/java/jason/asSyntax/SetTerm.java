package jason.asSyntax;

import java.util.Collection;

/**
 * The interface for sets in the AgentSpeak language
 *
 * @author Jomi
 */
public interface SetTerm extends Term, Collection<Term>, Iterable<Term> {

    /*public int  size();
    public boolean add(Term t);
    public boolean remove(Term t);
    public boolean contains(Term t);*/

    public void union(Iterable<Term> lt);
    public void intersection(Collection<Term> lt);
    public void difference(Collection<Term> lt);   
}
