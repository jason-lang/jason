package jason.pl;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jason.asSemantics.CircumstanceListener;
import jason.asSyntax.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.JasonException;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.util.Config;
import jason.util.ToDOM;

/** Represents a set of plans used by an agent */
public class PlanLibrary implements Iterable<Plan>, Serializable, ToDOM {

    @Serial
    private static final long serialVersionUID = 1913142118716665555L;

    public static String KQML_PLANS_FILE = "kqmlPlans.asl";

    /** a MAP from TE to a list of relevant plans */
    private final Map<PredicateIndicator,List<Plan>> relPlans = new ConcurrentHashMap<>();

    /**
     * All plans as defined in the AS code (maintains the order of the plans)
     */
    private final List<Plan> plans = new ArrayList<>();

    /** list of plans that have var as TE */
    private final List<Plan> varPlans = new ArrayList<>();

    /** A map from labels to plans */
    private final Map<String,Plan> planLabels = new ConcurrentHashMap<>();

    private boolean hasMetaEventPlans = false;
    private boolean hasJagPlans = false; // plans for sleep/wake signals

    private static final AtomicInteger lastPlanLabel = new AtomicInteger(0);

    private boolean hasUserKqmlReceived = false;

    //private Logger logger = Logger.getLogger(PlanLibrary.class.getName());

    private final transient Object lockPL;

    private PlanLibrary father = null;

    private boolean hasPlansForUpdateEvents = false;

    private Queue<PlanLibraryListener> listeners = new ConcurrentLinkedQueue<>();

    public PlanLibrary() {
        lockPL = new Object();
    }

    public PlanLibrary(PlanLibrary father) {
        this();
        this.father = father;
    }

    public boolean isRoot() {
        return father == null;
    }
    public PlanLibrary getFather() {
        return father;
    }

    public void setFather(PlanLibrary pl) {
        father = pl;
    }

    public boolean hasPlansForUpdateEvents() {
        return hasPlansForUpdateEvents;
    }

    public Object getLock() {
        return lockPL;
    }

    @Serial
    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
    }

    /**
     *  Add a new plan in PL. The source
     *  normally is "self" or the agent that sent this plan.
     *  If the PL already has a plan equals to the parameter p, only a
     *  new source is added.
     * <p>
     *  If <i>before</i> is true, the plan will be added in the
     *  beginning of the PlanLibrary; otherwise, it is added in
     *  the end.
     * <p>
     *  returns the plan just added
     */
    public Plan add(Plan p, Term source, boolean before) throws JasonException {
        synchronized (lockPL) {
            int i = plans.indexOf(p);
            if (i < 0) {
                // add label, if necessary
                if (p.getLabel() == null)
                    p.setLabel(getUniqueLabel());
                p.addSource(source);
                add(p, before);
            } else {
                p = plans.get(i);
                p.addSource(source);
            }
            return p;
        }
    }

    public Plan add(Plan p) throws JasonException {
        return add(p,false);
    }

    private final String kqmlReceivedFunctor = Config.get().getKqmlFunctor();

    /**
     * Adds a plan into the plan library, either before or after all other
     * plans depending on the boolean parameter.
     *
     * @param p The plan to be added to the plan library
     * @param before Whether to place the new plan before others
     */
    public Plan add(Plan p, boolean before) throws JasonException {
        p.setScope(this);
        synchronized (lockPL) {
            // test p.label
            if (p.getLabel() != null && planLabels.containsKey( getStringForLabel(p.getLabel()))) {
                // test if the new plan is equal, in this case, just add a source
                Plan planInPL = get(p.getLabel());
                if (p.equals(planInPL)) {
                    planInPL.addSource(p.getLabel().getSources().get(0));
                    return planInPL;
                } else {
                    throw new JasonException("There already is a plan with label " + p.getLabel());
                }
            }

            // add label, if necessary
            if (p.getLabel() == null)
                p.setLabel(getUniqueLabel());

            // add self source
            if (!p.hasSource())
                p.getLabel().addAnnot(BeliefBase.TSelf);

            // add source file
            if (p.getSourceFile() != null && !p.getSourceFile().isEmpty())
                p.getLabel().addAnnot(ASSyntax.createStructure("url", ASSyntax.createString( p.getSourceFile())));


            Trigger pte = p.getTrigger();
            if (pte.getLiteral().getFunctor().equals(kqmlReceivedFunctor)) {
                // is it a KQML plan from a file different from the one provided by Jason?
                if (! (p.getSrcInfo() != null && KQML_PLANS_FILE.equals(p.getSrcInfo().getSrcFile()))) {
//                if (! (p.getSrcInfo() != null && p.getSrcInfo().getSrcFile().endsWith(".jar!/asl/kqmlPlans.asl"))) {
                    hasUserKqmlReceived = true;
                }
            }

            p.setAsPlanTerm(false); // it is not a term anymore

            planLabels.put( getStringForLabel(p.getLabel()), p);

            if (pte.getLiteral().isVar() || pte.getLiteral().getNS().isVar()) {
                if (before)
                    varPlans.add(0,p);
                else
                    varPlans.add(p);
                // add plan p in all entries
                for (List<Plan> lp: relPlans.values())
                    if (!lp.isEmpty() && lp.get(0).getTrigger().sameType(pte)) // only add if same type
                        if (before)
                            lp.add(0,p);
                        else
                            lp.add(p);
            } else {
                if (pte.isUpdate())
                    hasPlansForUpdateEvents = true;

                List<Plan> codesList = relPlans.get(pte.getPredicateIndicator());
                if (codesList == null) {
                    codesList = new ArrayList<>();
                    // copy plans from var plans
                    for (Plan vp: varPlans)
                        if (vp.getTrigger().sameType(pte))
                            codesList.add(vp);
                    relPlans.put(pte.getPredicateIndicator(), codesList);
                }
                if (before)
                    codesList.add(0,p);
                else
                    codesList.add(p);
            }

            if (pte.getOperator() == TEOperator.goalState)
                hasMetaEventPlans = true;
            if (pte.equals(TE_JAG_AWAKING) || pte.equals(TE_JAG_SHUTTING_DOWN) || pte.equals(TE_JAG_SLEEPING))
                hasJagPlans = true;

            if (before)
                plans.add(0,p);
            else
                plans.add(p);

            if (hasListener())
                for (PlanLibraryListener l : listeners)
                    l.planAdded(p);
            return p;
        }
    }

    public void addAll(PlanLibrary pl) throws JasonException {
        synchronized (lockPL) {
            for (Plan p: pl) {
                add(p, false);
            }
        }
        eDOMPlans = null;
    }

    public void addAll(List<Plan> plans) throws JasonException {
        synchronized (lockPL) {
            for (Plan p: plans) {
                add(p, false);
            }
        }
    }

    private String getStringForLabel(Literal p) {
        // use functor + terms
        StringBuilder l = new StringBuilder();
        if (p.getNS() != Literal.DefaultNS)
            l.append(p.getNS()).append("::");
        l.append(p.getFunctor());
        if (p.hasTerm())
            for (Term t: p.getTerms())
                l.append(t.toString());
        return l.toString();
    }

    public boolean hasMetaEventPlans() {
        return hasMetaEventPlans;
    }

    public boolean hasUserKqmlReceivedPlans() {
        return hasUserKqmlReceived;
    }

    public boolean hasJagPlans() {
        return hasJagPlans;
    }

    /** add a label to the plan */
    public Pred getUniqueLabel() {
        String l;
        do {
            l = "p__" + (lastPlanLabel.incrementAndGet());
        } while (planLabels.containsKey(l));
        return new Pred(l);
    }

    /** return a plan for a label */
    public Plan get(String label) {
        return get(new Atom(label));
    }
    /** return a plan for a label */
    public Plan get(Literal label) {
        return planLabels.get(getStringForLabel(label));
    }

    public int size() {
        return plans.size();
    }

    public List<Plan> getPlans() {
        return plans;
    }

    public Iterator<Plan> iterator() {
        return plans.iterator();
    }

    /** remove all plans */
    public void clear() {
        planLabels.clear();
        plans.clear();
        varPlans.clear();
        relPlans.clear();
        eDOMPlans = null;
    }

    /**
     * Remove a plan represented by the label <i>pLabel</i>.
     * In case the plan has many sources, only the plan's source is removed.
     */
    public boolean remove(Literal pLabel, Term source) {
        // find the plan
        Plan p = get(pLabel);
        if (p != null) {
        	eDOMPlans = null;

        	boolean hasSource = p.delSource(source);

            // if no source anymore, remove the plan
            if (hasSource && !p.hasSource()) {
                remove(pLabel);
            }
            return true;
        }
        return false;
    }

    /** remove the plan with label <i>pLabel</i> */
    public Plan remove(Literal pLabel) {
    	eDOMPlans = null;
    	synchronized (lockPL) {
            Plan p = planLabels.remove( getStringForLabel(pLabel) );

            // remove it from plans' list
            plans.remove(p);

            if (p.getTrigger().getLiteral().isVar()) {
                varPlans.remove(p);
                // remove p from all entries and
                // clean empty entries
                Iterator<PredicateIndicator> ipi = relPlans.keySet().iterator();
                while (ipi.hasNext()) {
                    PredicateIndicator pi = ipi.next();
                    List<Plan> lp = relPlans.get(pi);
                    lp.remove(p);
                    if (lp.isEmpty()) {
                        ipi.remove();
                    }
                }
            } else {
                List<Plan> codesList = relPlans.get(p.getTrigger().getPredicateIndicator());
                codesList.remove(p);
                if (codesList.isEmpty()) {
                    // no more plans for this TE
                    relPlans.remove(p.getTrigger().getPredicateIndicator());
                }
            }

            if (hasListener())
                for (PlanLibraryListener l : listeners)
                    l.planRemoved(p);

            return p;
        }
    }

    public boolean hasCandidatePlan(Trigger te) {
        if (te == null)
            return false;
        else
            return getCandidatePlans(te) != null;
    }


    public List<Plan> getCandidatePlans(Trigger te) {
        synchronized (lockPL) {
            List<Plan> l = null;
            if (te.getLiteral().isVar() || te.getNS().isVar()) { // add all plans!
                for (Plan p: this)
                    if (p.getTrigger().sameType(te)) {
                        if (l == null)
                            l = new ArrayList<>();
                        l.add(p);
                    }
            } else {
                l = relPlans.get(te.getPredicateIndicator());
                if ((l == null || l.isEmpty()) && !varPlans.isEmpty() && te != TE_JAG_SLEEPING && te != TE_JAG_AWAKING) {  // no rel plan, try varPlan
                    for (Plan p: varPlans)
                        if (p.getTrigger().sameType(te)) {
                            if (l == null)
                                l = new ArrayList<>();
                            l.add(p);
                        }
                }
            }
            return l; // if no rel plan, have to return null instead of empty list
        }
    }

    public static final Trigger TE_JAG_SLEEPING      = new Trigger(TEOperator.add, TEType.belief, new Atom("jag_sleeping"));
    public static final Trigger TE_JAG_AWAKING       = new Trigger(TEOperator.add, TEType.belief, new Atom("jag_awaking"));
    public static final Trigger TE_JAG_SHUTTING_DOWN = new Trigger(TEOperator.add, TEType.belief, new LiteralImpl("jag_shutting_down"));

    public PlanLibrary clone() {
        PlanLibrary pl = new PlanLibrary();
        try {
            synchronized (lockPL) {
                for (Plan p: this) {
                    pl.add((Plan)p.clone(), false);
                }
            }
        } catch (JasonException e) {
            e.printStackTrace();
        }
        return pl;
    }


    public boolean hasListener() {
        return !listeners.isEmpty();
    }
    public void addListener(PlanLibraryListener el) {
        listeners.add(el);
    }

    public void removeListener(PlanLibraryListener el) {
        if (el != null) {
            listeners.remove(el);
        }
    }

    public String toString() {
        return plans.toString();
    }

    /** get as txt */
    public String getAsTxt(boolean includeKQMLPlans) {
        Map<String, StringBuilder> sPlans = new HashMap<>();
        StringBuilder r;
        for (Plan p: plans) {
            r = sPlans.get(p.getSourceFile());
            if (r == null) {
                r = new StringBuilder();
                if (p.getSourceFile().isEmpty()) {
                    r.append("\n\n// plans without file\n\n");
                } else {
                    r.append("\n\n// plans from ").append(p.getSourceFile()).append("\n\n");
                }
                sPlans.put(p.getSourceFile(), r);
            }
            r.append(p).append("\n");
        }

        r = new StringBuilder();
        StringBuilder end = new StringBuilder("\n");
        for (String f: sPlans.keySet()) {
            if (f.contains("kqmlPlans"))
                if (includeKQMLPlans)
                    end.append(sPlans.get(f));
                else
                    continue;
            if (f.isEmpty())
                end.append(sPlans.get(f));
            else
                r.append(sPlans.get(f));
        }
        return r.toString()+end;
    }

    // for cache
    protected Element eDOMPlans = null;

    /** get as XML */
    public Element getAsDOM(Document document) {
        if (eDOMPlans != null)
            return eDOMPlans;

        eDOMPlans = document.createElement("plans");
        String lastFunctor = null;
        synchronized (lockPL) {
            for (Plan p: plans) {
                String currentFunctor = p.getTrigger().getLiteral().getFunctor();
                if (lastFunctor != null && !currentFunctor.equals(lastFunctor)) {
                    eDOMPlans.appendChild(document.createElement("new-set-of-plans"));
                }
                lastFunctor = currentFunctor;
                eDOMPlans.appendChild(p.getAsDOM(document));
            }
        }
        return eDOMPlans;
    }
}
