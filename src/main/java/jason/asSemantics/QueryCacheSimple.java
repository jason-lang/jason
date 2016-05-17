package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.profiling.QueryProfiling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QueryCacheSimple {

    private QueryProfiling prof;
    
    private Map<Literal, List<Unifier>> cache = null;
    protected Logger logger = null;
    
    public QueryCacheSimple(Agent ag, QueryProfiling p) {
        this.prof = p;
        logger    = Logger.getLogger(QueryCacheSimple.class.getName()+"-"+ag.getTS().getUserAgArch().getAgName());
        cache     = new HashMap<Literal,List<Unifier>>();
    }
    
    public void reset() {
        cache.clear();
    }
    
    public Iterator<Unifier> getCache(final Literal f) {
        List<Unifier> l = cache.get(f);
        if (l == null)
            return null;
        if (prof != null)
            prof.incHits();
        return l.iterator();
    }

    public void queryFinished(Literal f, List<Unifier> r) {
        //System.out.println("finished "+f+" with "+r);
        cache.put(f, r);
    }

}
