package jason.asSyntax;

import java.util.Collection;
import java.util.Set;

/**
 * The interface for maps in the AgentSpeak language
 *
 * @author Jomi
 */
public interface MapTerm extends Term {
    public int size();
    public void clear();
    public Term get(Term k);
    public void put(Term k, Term v);
    public Term remove(Term k);
    public Set<Term> keys();
    public Collection<Term> values();
    public MapTerm deepClone();
}
