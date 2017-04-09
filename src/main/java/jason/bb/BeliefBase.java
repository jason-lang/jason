package jason.bb;

import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;


/**
 * Common interface for all kinds of Jason Belief bases, even those
 * customised by the user.
 */
public abstract class BeliefBase implements Iterable<Literal>, Cloneable {

    public static final Term ASelf    = new Atom("self");
    public static final Term APercept = new Atom("percept");

    /** represents the structure 'source(percept)' */
    public static final Term TPercept = Pred.createSource(APercept);

    /** represents the structure 'source(self)' */
    public static final Term TSelf    = Pred.createSource(ASelf);

    /**
     * Called before the MAS execution with the agent that uses this
     * BB and the args informed in .mas2j project.<br>
     * Example in .mas2j:<br>
     *     <code>agent BeliefBaseClass(1,bla);</code><br>
     * the init args will be ["1", "bla"].
     */
    public void init(Agent ag, String[] args) {}

    /** Called just before the end of MAS execution */
    public void stop() {}

    /** removes all beliefs from BB */
    public void clear() {}

    public Set<Atom> getNameSpaces() {
        return null;
    }

    /** Adds a belief in the end of the BB, returns true if succeed.
     *  The annots of l may be changed to reflect what was changed in the BB,
     *  for example, if l is p[a,b] in a BB with p[a], l will be changed to
     *  p[b] to produce the event +p[b], since only the annotation b is changed
     *  in the BB. */
    public boolean add(Literal l) {
        return false;
    }

    /** Adds a belief in the BB at <i>index</i> position, returns true if succeed */
    public boolean add(int index, Literal l) {
        return false;
    }


    /** Returns an iterator for all beliefs. */
    public abstract Iterator<Literal> iterator();

    /**
     * Returns an iterator for all literals in the default namespace of the BB that match the functor/arity
     * of the parameter.<br>
     */
    public abstract Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi);

    /**
     * Returns an iterator for all literals relevant for l's predicate
     * indicator, if l is a var, returns all beliefs.<br>
     *
     * The unifier <i>u</i> may contain values for variables in <i>l</i>.
     *
     * Example, if BB={a(10),a(20),a(2,1),b(f)}, then
     * <code>getCandidateBeliefs(a(5), {})</code> = {{a(10),a(20)}.<br>
     * if BB={a(10),a(20)}, then <code>getCandidateBeliefs(X)</code> =
     * {{a(10),a(20)}. The <code>getCandidateBeliefs(a(X), {X -> 5})</code>
     * should also return {{a(10),a(20)}.<br>
     */
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        return null;
    }

    /**
     * Returns the literal l as it is in BB, this method does not
     * consider annotations in the search. <br> Example, if
     * BB={a(10)[a,b]}, <code>contains(a(10)[d])</code> returns
     * a(10)[a,b].
     */
    public Literal contains(Literal l) {
        return null;
    }

    /** Returns the number of beliefs in BB */
    public int size() {
        return 0;
    }

    /** Returns all beliefs that have "percept" as source */
    public Iterator<Literal> getPercepts() {
        return null;
    }

    /** Removes a literal from BB, returns true if succeed */
    public boolean remove(Literal l) {
        return false;
    }

    /** Removes all believes with some functor/arity in the default namespace */
    public boolean abolish(PredicateIndicator pi) {
        return abolish(Literal.DefaultNS, pi);
    }
    public boolean abolish(Atom namespace, PredicateIndicator pi) {
        return false;
    }

    /** Gets the BB as XML */
    public Element getAsDOM(Document document) {
        return null;
    }

    public abstract BeliefBase clone();

    Object lock = new Object();

    /** Gets a lock for the BB */
    public Object getLock() {
        return lock;
    }
}
