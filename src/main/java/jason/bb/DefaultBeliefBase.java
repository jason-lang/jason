package jason.bb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;

/**
 * Default implementation of Jason BB.
 */
public class DefaultBeliefBase extends BeliefBase {

    private static Logger logger = Logger.getLogger(DefaultBeliefBase.class.getSimpleName());

    /**
     * belsMap is a table where the key is the bel.getFunctorArity and the value
     * is a list of literals with the same functorArity.
     */
    private Map<PredicateIndicator, BelEntry> belsMapDefaultNS = new ConcurrentHashMap<PredicateIndicator, BelEntry>();

    private Map<Atom, Map<PredicateIndicator, BelEntry>> nameSpaces = new ConcurrentHashMap<Atom, Map<PredicateIndicator,BelEntry>>();

    private int size = 0;

    /** set of beliefs with percept annot, used to improve performance of buf */
    protected Set<Literal> percepts = new HashSet<Literal>();

    public DefaultBeliefBase() {
        nameSpaces.put(Literal.DefaultNS, belsMapDefaultNS);
    }

    @Override
    public void init(Agent ag, String[] args) {
        if (ag != null) {
            logger = Logger.getLogger(ag.getTS().getUserAgArch().getAgName() + "-"+DefaultBeliefBase.class.getSimpleName());
        }
    }

    @Override
    public Set<Atom> getNameSpaces() {
        return nameSpaces.keySet();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
        percepts.clear();
        belsMapDefaultNS.clear();
        nameSpaces.clear();
        nameSpaces.put(Literal.DefaultNS, belsMapDefaultNS);
    }

    @Override
    public Iterator<Literal> getPercepts() {
        final Iterator<Literal> i = percepts.iterator();
        return new Iterator<Literal>() {
            Literal current = null;
            public boolean hasNext() {
                return i.hasNext();
            }
            public Literal next() {
                current = i.next();
                return current;
            }
            public void remove() {
                if (current == null) {
                    logger.warning("Trying to remove a perception, but the the next() from the iterator is not called before!");
                }
                // remove from percepts
                i.remove();

                // remove the percept annot
                current.delAnnot(BeliefBase.TPercept);

                // and also remove from the BB
                removeFromEntry(current);
            }
        };
        //return ((Set<Literal>)percepts.clone()).iterator();
    }

    Set<Literal> getPerceptsSet() {
        return percepts;
    }

    @Override
    public boolean add(Literal l) {
        return add(l, false);
    }

    @Override
    public boolean add(int index, Literal l) {
        return add(l, index != 0);
    }

    protected boolean add(Literal l, boolean addInEnd) {
        if (!l.canBeAddedInBB()) {
            logger.log(Level.SEVERE, "Error: '"+l+"' can not be added in the belief base.");
            return false;
        }

        Literal bl = contains(l);
        if (bl != null && !bl.isRule()) {
            // add only annots
            if (bl.importAnnots(l)) {
                // check if it needs to be added in the percepts list
                // (note that l contains only the annots imported)
                if (l.hasAnnot(TPercept)) {
                    percepts.add(bl);
                }
                return true;
            }
        } else {
            // new bel

            l = l.copy(); // we need to clone l for the consequent event to not have a ref to this bel (which may change before the event is processed); see bug from Viviana Marcardi
            BelEntry entry = provideBelEntry(l);
            entry.add(l, addInEnd);

            // add it in the percepts list
            if (l.hasAnnot(TPercept)) {
                percepts.add(l);
            }

            size++;
            return true;
        }
        return false;
    }

    private BelEntry provideBelEntry(Literal l) {
        Map<PredicateIndicator, BelEntry> belsMap = belsMapDefaultNS;
        if (l.getNS() != Literal.DefaultNS) {
            belsMap = nameSpaces.get(l.getNS());
            if (belsMap == null) {
                belsMap = new ConcurrentHashMap<PredicateIndicator, DefaultBeliefBase.BelEntry>();
                nameSpaces.put(l.getNS(), belsMap);
            }
        }
        BelEntry entry = belsMap.get(l.getPredicateIndicator());
        if (entry == null) {
            entry = new BelEntry();
            belsMap.put(l.getPredicateIndicator(), entry);
        }
        return entry;
    }

    @Override
    public boolean remove(Literal l) {
        Literal bl = contains(l);
        if (bl != null) {
            if (l.hasSubsetAnnot(bl)) { // e.g. removing b[a] or b[a,d] from BB b[a,b,c]
                // second case fails
                if (l.hasAnnot(TPercept)) {
                    percepts.remove(bl);
                }
                boolean result = bl.delAnnots(l.getAnnots()); // note that l annots can be empty, in this case, nothing is deleted!
                return removeFromEntry(bl) || result;
            }
        }
        return false;
    }

    private boolean removeFromEntry(Literal l) {
        if (l.hasSource()) {
            return false;
        } else {
            Map<PredicateIndicator, BelEntry> belsMap = l.getNS() == Literal.DefaultNS ? belsMapDefaultNS : nameSpaces.get(l.getNS());
            PredicateIndicator key = l.getPredicateIndicator();
            BelEntry entry = belsMap.get(key);
            entry.remove(l);
            if (entry.isEmpty()) {
                belsMap.remove(key);
            }
            size--;
            return true;
        }
    }

    @Override
    public Iterator<Literal> iterator() {
        final Iterator<Map<PredicateIndicator, BelEntry>> ins = nameSpaces.values().iterator();
        return new Iterator<Literal>() {

            Iterator<BelEntry> ibe = ins.next().values().iterator();
            Iterator<Literal>  il  = null;
            Literal            l   = null;
            {
                goNext();
            }

            public boolean hasNext() {
                return il != null && il.hasNext(); //l != null;
            }

            private void goNext() {
                while (il == null || !il.hasNext()) {
                    if (ibe.hasNext()) {
                        il = ibe.next().list.iterator();
                    } else if (ins.hasNext()) {
                        ibe = ins.next().values().iterator();
                    } else {
                        return;
                    }
                }
            }

            public Literal next() {
                l = il.next(); // store it for remove
                goNext();
                return l;
            }

            public void remove() {
                il.remove();
                if (l.hasAnnot(TPercept)) {
                    percepts.remove(l);
                }
                size--;
            }
        };
    }

    @Override
    public boolean abolish(Atom namespace, PredicateIndicator pi) {
        BelEntry entry = nameSpaces.get(namespace).remove(pi);
        if (entry != null) {
            size -= entry.size();

            // remove also in percepts list!
            Iterator<Literal> i = percepts.iterator();
            while (i.hasNext()) {
                Literal l = i.next();
                if (l.getPredicateIndicator().equals(pi))
                    i.remove();
            }
            return true;
        } else {
            return false;
        }
        //return belsMap.remove(pi) != null;
    }

    @Override
    public Literal contains(Literal l) {
        Map<PredicateIndicator, BelEntry> belsMap = l.getNS() == Literal.DefaultNS ? belsMapDefaultNS : nameSpaces.get(l.getNS());
        if (belsMap == null)
            return null;
        BelEntry entry = belsMap.get(l.getPredicateIndicator());
        if (entry == null) {
            return null;
        } else {
            //logger.info("*"+l+":"+l.hashCode()+" = "+entry.contains(l)+" in "+this);//+" entry="+entry);
            return entry.contains(l);
        }
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {
        Map<PredicateIndicator, BelEntry> pi2entry = nameSpaces.get(pi.getNS());
        if (pi2entry == null )
            return null;

        BelEntry entry = pi2entry.get(pi);
        if (entry != null)
            return entry.list.iterator();
        else
            return null;
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        if (l.isVar()) {
            // all bels are relevant
            return iterator();
        } else {
            Map<PredicateIndicator, BelEntry> belsMap = belsMapDefaultNS;
            if (l.getNS() != Literal.DefaultNS) {
                Atom ns = l.getNS();
                if (ns.isVar()) {
                    l = (Literal)l.capply(u);
                    ns = l.getNS();
                }
                if (ns.isVar()) { // still a var
                    return iterator();
                }
                belsMap = nameSpaces.get(ns);
            }
            if (belsMap == null)
                return null;
            BelEntry entry = belsMap.get(l.getPredicateIndicator());
            if (entry != null) {
                //System.out.println(l.getNS() + "::::"+ l+ "  ==> " + entry.list);
                return entry.list.iterator();
            } else {
                return null;
            }
        }
    }

    public String toString() {
        return nameSpaces.toString();
    }

    @Override
    public BeliefBase clone() {
        DefaultBeliefBase bb = new DefaultBeliefBase();
        for (Literal b: this) {
            bb.add(1, b.copy());
        }
        return bb;
    }

    @Override
    public Element getAsDOM(Document document) {
        int tries = 0;
        Element ebels = null;
        List<Literal> allBels;
        while (tries < 10) { // max 10 tries
            try {
                synchronized (getLock()) {
                    ebels = (Element) document.createElement("beliefs");
                    Element enss = (Element) document.createElement("namespaces");
                    Element ens = (Element) document.createElement("namespace");
                    ens.setAttribute("id", Literal.DefaultNS.toString()); // to ensure default is the first
                    enss.appendChild(ens);
                    for (Atom ns: getNameSpaces()) {
                        if (ns == Literal.DefaultNS)
                            continue;
                        ens = (Element) document.createElement("namespace");
                        ens.setAttribute("id", ns.getFunctor());
                        enss.appendChild(ens);
                    }
                    ebels.appendChild(enss);
                    // copy bels to an array to sort it
                    allBels = new ArrayList<Literal>(size());
                    for (Literal l: this)
                        allBels.add(l);
                }
                Collections.sort(allBels);
                for (Literal l: allBels)
                    ebels.appendChild(l.getAsDOM(document));
                break; // quit the loop
            } catch (Exception e) { // normally concurrent modification, but others happen
                tries++;
                e.printStackTrace();
                // simply tries again
            }
        }
        return ebels;
    }

    /** each predicate indicator has one BelEntry assigned to it */
    final class BelEntry {

        final private Deque<Literal> list = new LinkedBlockingDeque<Literal>();  // maintains the order of the beliefs
        final private Map<StructureWrapperForLiteral,Literal> map = new ConcurrentHashMap<StructureWrapperForLiteral,Literal>(); // to find content faster

        public void add(Literal l, boolean addInEnd) {
            map.put(new StructureWrapperForLiteral(l), l);
            if (addInEnd) {
                list.addLast(l);
            } else {
                list.addFirst(l);
            }
        }

        public void remove(Literal l) {
            Literal linmap = map.remove(new StructureWrapperForLiteral(l));
            if (linmap != null) {
                list.remove(linmap);
            }
        }

        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public Literal contains(Literal l) {
            return map.get(new StructureWrapperForLiteral(l));
        }

        protected Object clone() {
            BelEntry be = new BelEntry();
            for (Literal l: list) {
                be.add(l.copy(), false);
            }
            return be;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            for (Literal l: list) {
                s.append(l+":"+l.hashCode()+",");
            }
            return s.toString();
        }
    }
}
