import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.bb.BeliefBase;


/**
  * Void Belief Base: store nothing!
  */
public class VoidBB extends jason.bb.BeliefBase {

    public Iterator<Literal> getPercepts() {
        return new ArrayList<Literal>().iterator();
    }

    public boolean add(Literal l) {
        return true;
    }

    public boolean add(int index, Literal l) {
        return true;
    }

    public Literal contains(Literal l) {
        return l;
    }

    @Override
    public Set<Atom> getNameSpaces() {
        return new HashSet<Atom>();
    }

    public Iterator<Literal> iterator() {
        return new ArrayList<Literal>().iterator();
    }

    public boolean remove(Literal l) {
        return true;
    }

    public boolean abolish(PredicateIndicator pi) {
        return true;
    }

    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {
        return new ArrayList<Literal>().iterator();
    }

    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        return new ArrayList<Literal>().iterator();
    }

    public BeliefBase clone()  {
        return new VoidBB();
    }
}
