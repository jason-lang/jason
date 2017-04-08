package jason.bb;

import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;

/**

 This class is to be extended by customised belief bases that may be
 used in chains (of responsibility).

 For example, the code
 <pre>
 BeliefBase bb =
    new MyBB1(
       new MyBB2());
      // DefaultBeliefBase is the next of the last element of the chain
 </pre>
 will chain 3 BBs: MyBB1, myBB2, and the DefaultBeliefBase. So, for any operation of
 the BB interface, the operation is firstly called in MyBB1, then in MyBB2 and finally
 in the DefaultBeliefBase.

 The code of MyBB1 looks like:

 <pre>
 class MyBB1 extends ChainBBAdapter {
    public MyBB1() { }
    public MyBB1(BeliefBase next) {
        super(next);
    }

    public boolean add(Literal l) {
        ... some customisation of add ....
        return next.add(l); // delegate the operation for the next BB in the chain
    }

    ... customisation of other operations ...
 }
 </pre>

 @navassoc - nextBB - BeliefBase

 @author Jomi

 */
public abstract class ChainBBAdapter extends BeliefBase {

    protected BeliefBase nextBB = null; // the next BB in the chain

    public ChainBBAdapter() {
        nextBB = new DefaultBeliefBase();
    }
    public ChainBBAdapter(BeliefBase bb) {
        nextBB = bb;
    }

    public void setNext(BeliefBase bb) {
        nextBB = bb;
    }

    public ChainBBAdapter getNextAdapter() {
        return nextBB instanceof ChainBBAdapter ? (ChainBBAdapter)nextBB : null;
    }

    public BeliefBase getLastBB() {
        if (nextBB == null)
            return this;
        else if (nextBB instanceof ChainBBAdapter)
            return ((ChainBBAdapter)nextBB).getLastBB();
        else
            return nextBB;
    }

    // Methods of BB interface

    @Override
    public void init(Agent ag, String[] args) {
        nextBB.init(ag, args);
    }
    @Override
    public void stop() {
        nextBB.stop();
    }

    @Override
    public void clear() {
        nextBB.clear();
    }

    @Override
    public Set<Atom> getNameSpaces() {
        return nextBB.getNameSpaces();
    }

    @Override
    public boolean add(Literal l) {
        return nextBB.add(l);
    }

    @Override
    public boolean add(int index, Literal l) {
        return nextBB.add(index, l);
    }

    @Override
    public Literal contains(Literal l) {
        return nextBB.contains(l);
    }

    @Override
    public Iterator<Literal> iterator() {
        return nextBB.iterator();
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {
        return nextBB.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        return nextBB.getCandidateBeliefs(l, u);
    }

    @Override
    public Iterator<Literal> getPercepts() {
        return nextBB.getPercepts();
    }

    @Override
    public boolean abolish(PredicateIndicator pi) {
        return nextBB.abolish(pi);
    }

    @Override
    public boolean remove(Literal l) {
        return nextBB.remove(l);
    }

    @Override
    public int size() {
        return nextBB.size();
    }

    @Override
    public Element getAsDOM(Document document) {
        return nextBB.getAsDOM(document);
    }

    @Override
    public BeliefBase clone() {
        return this;
    }

    @Override
    public String toString() {
        return nextBB.toString();
    }
}
