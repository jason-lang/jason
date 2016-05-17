package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.profiling.QueryProfiling;
import jason.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class QueryCacheAdv {

    private Agent ag; 
    private Map<PredicateIndicator, List<Pair<Literal,List<Unifier>>>> cache = null;
    private Map<PredicateIndicator, List<Pair<Literal,List<Unifier>>>> tmp   = null;
    private Set<QueryCacheKey> noCache = null;

    private QueryProfiling prof;
    
    protected Logger logger = null;
    
    public QueryCacheAdv(Agent ag, QueryProfiling p) {
        this.ag = ag;
        this.prof = p;
        logger  = Logger.getLogger(QueryCacheAdv.class.getName()+"-"+ag.getTS().getUserAgArch().getAgName());
        cache   = new HashMap<PredicateIndicator, List<Pair<Literal,List<Unifier>>>>();
        tmp     = new HashMap<PredicateIndicator, List<Pair<Literal,List<Unifier>>>>();
        noCache = new HashSet<QueryCacheKey>();
    }
    
    public void reset() {
        cache.clear();
        tmp.clear();
        noCache.clear();
    }
    
    public Pair<Literal,Iterator<Unifier>> getCache(final Literal f) {
        List<Pair<Literal,List<Unifier>>> opts = cache.get(f.getPredicateIndicator());
        if (opts != null) {
            //System.out.println("options for "+f+" are "+opts);
            // TODO: sort the opts lists as more specific are tried first
            for (Pair<Literal,List<Unifier>> ic: opts) { // for each possible entry in the cache
                //System.out.println("  try opt "+ic+" "+new Unifier().unifies(f, ic.getFirst()) + "  "+f.isMoreGeneral(ic.getFirst()));
                if (new Unifier().unifies(f, ic.getFirst()) && ic.getFirst().subsumes(f)) { 
                    if (prof != null)
                        prof.incHits();
                    //System.out.println("reuse "+opts+" for "+f);
                    return new Pair<Literal,Iterator<Unifier>>(ic.getFirst(),ic.getSecond().iterator());
                    
                }
            }
        }

        List<Pair<Literal,List<Unifier>>> optsTmp = tmp.get(f.getPredicateIndicator());
        if (optsTmp != null && !noCache.contains(new QueryCacheKey(f))) {
            for (Pair<Literal,List<Unifier>> ic: optsTmp) { // for each possible entry in the cache
                //System.out.println("  try opt tmp "+ic+" "+new Unifier().unifies(f, ic.getFirst()) + "  "+f.subsumes(ic.getFirst()));
                if (new Unifier().unifies(f, ic.getFirst()) && ic.getFirst().subsumes(f)) { 
                    //System.out.println("    potential use for "+f+" with "+ic);
                    if (prof != null)
                        prof.incHits();
                    final Literal       lTmp    = ic.getFirst();
                    final List<Unifier> listTmp = ic.getSecond();
                    final int           listSize = listTmp.size();

                    noCache.add(new QueryCacheKey(lTmp));
                    
                    // use already obtained solutions
                    return new Pair<Literal,Iterator<Unifier>>(lTmp,new Iterator<Unifier>() {
                        Iterator<Unifier> i       = null;
                        int               iTmp    = 0;
                        boolean           fromTmp = true;
                        
                        public boolean hasNext() {
                            boolean hn = iTmp < listSize;
                            if (hn) {
                                return true;
                            } else if (fromTmp) {
                                fromTmp = false;
                                //System.out.println("now on from logCons for "+f+" using "+ct.getFirst());
                                // stop using tmp and use logCons
                                i = lTmp.logicalConsequence(ag, new Unifier());
                                // skip the elements in tmp cache
                                for (int c=0; c<listSize; c++)
                                    i.next();
                            }
                            hn = i != null && i.hasNext(); // use new iterator to compute hasNext
                            if (!hn) {
                                queryFinished(lTmp);
                                //noCache.remove(new CacheKey(lTmp));
                            }
                            return hn;
                        }
                        public Unifier next() {
                            if (fromTmp) {
                                //System.out.println(" from tmp "+listTmp.get(iTmp)+" cache="+fromTmp);
                                return listTmp.get(iTmp++);
                            } else {
                                Unifier a = i.next();
                                //System.out.println(" from tmp "+a+" cache="+fromTmp);
                                listTmp.add(a);
                                return a;
                            }
                        }
                        public void remove() { }
                    });
                
                }
            }
        }
        return null;
    }

    public void addAnswer(Literal f, Unifier a) {
        if (noCache.contains(new QueryCacheKey(f)))
            return;
        
        List<Unifier> ans = null;
        List<Pair<Literal,List<Unifier>>> opts = tmp.get(f.getPredicateIndicator());
        if (opts == null) {
            opts = new ArrayList<Pair<Literal,List<Unifier>>>();
            tmp.put(f.getPredicateIndicator(), opts);
        } else {
            for (Pair<Literal,List<Unifier>> ic: opts) { // for each possible entry in the cache
                if (f.equals(ic.getFirst())) {
                    ans = ic.getSecond();
                    break;
                }
            } 
        }
        if (ans == null) {
            ans = new ArrayList<Unifier>();
            opts.add(new Pair<Literal, List<Unifier>>(f,ans));
        }
        //System.out.println("   add "+a+" for "+f);
        ans.add(a);
    }
    
    
    public void queryFinished(Literal f) {
        List<Pair<Literal,List<Unifier>>> opts = tmp.get(f.getPredicateIndicator());
        if (opts != null) {
            Iterator<Pair<Literal,List<Unifier>>> i = opts.iterator();
            while (i.hasNext()) {
                Pair<Literal,List<Unifier>> ic = i.next();
                if (f.equals(ic.getFirst())) {
                    i.remove();
                    
                    List<Pair<Literal,List<Unifier>>> optsCache = cache.get(f.getPredicateIndicator());
                    if (optsCache == null) {
                        optsCache = new ArrayList<Pair<Literal,List<Unifier>>>();
                        cache.put(f.getPredicateIndicator(), optsCache);
                    }
                    optsCache.add(ic);
                    //System.out.println("finished "+f+" with "+ic);
                    return;                    
                }
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("In cache:\n");
        for (List<Pair<Literal,List<Unifier>>> q: cache.values()) {
            for (Pair<Literal,List<Unifier>> ic: q) {
                out.append("  "+ic.getFirst()+": "+ic.getSecond()+"\n");                
            }
        }
        out.append("In cache (but not finished):\n");
        for (List<Pair<Literal,List<Unifier>>> q: tmp.values()) {
            for (Pair<Literal,List<Unifier>> ic: q) {
                out.append("  "+ic.getFirst()+": "+ic.getSecond()+"\n");                
            }
        }
        return out.toString();
    }
    /*
    public Iterator<Unifier> queryFilter(final CacheKey f, final Iterator<Unifier> iun) {
        // store results in the cache
        return new Iterator<Unifier>() {
            public boolean hasNext() {
                if (iun.hasNext())
                    return true;
                else {
                    //ag.getLogger().info("finished query "+f);
                    queryFinished(f);
                    return false;
                }
            }
            public Unifier next() {
                Unifier n = iun.next();
                addAnswer(f, n);
                return n;
            }      
            public void remove() {   }
        };
    }
    */    
    //private  static Unifier emptyUnif = new Unifier();
    /*public CacheKey prepareForCache(Literal l, Unifier u) {
        nbQueries++;
      */  
        /*Map<VarTerm, Integer> all  = new HashMap<VarTerm, Integer>();
        l.countVars(all);
        Unifier newu = new Unifier();
        for (VarTerm v: u) {
            if (!v.isUnnamedVar() && all.containsKey(v)) {
                Term vl = u.get(v);
                if (vl != null)
                    newu.bind(v, u.get(v));
            }
        }
        //System.out.println(l+"="+u+" .... "+newu);
        return new Pair<LogicalFormula,Unifier>(l,newu);
        */
        /*l = l.copy();
        l.apply(u);
        */
        // TODO: use special compare, unvalued vars are equals.
        //return new Pair<LogicalFormula,Unifier>(new LiteralEqualWrapper(l),emptyUnif);
    /*    return new CacheKey(l,u);
    }*/
    /*
    public Pair prepareForCache(LogExpr l, Unifier u) {
        LogExpr nl = (LogExpr)l.clone();
        try {
            nl.apply(u);
            //ag.getLogger().info(l+" *** prep = "+nl);
            return nl;
        } catch (Exception e) {
            return null;
        }
    }*/
    
}
