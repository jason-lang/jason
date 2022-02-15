package jason.asSyntax;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSemantics.Unifier;


/**
 * Represents a Jason map as a TreeMap in java
 *
 *
 * @author Jomi
 */
public class MapTermImpl extends DefaultTerm implements MapTerm {

    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(SetTermImpl.class.getName());

    private Map<Term, Term> map;

    public MapTermImpl() {
        map = new TreeMap<>();
    }

    @Override
    public MapTerm clone() {
        return this;
        /* deep clone causes a problem for "for each" loops. so this code is commented.
         * and an internal action is used to create a copy of maps. (idem for sets and queues)

        MapTermImpl t = new MapTermImpl();
        for (Term k: this.map.keySet())
            t.map.put(k.clone(), map.get(k).clone());
        t.hashCodeCache = this.hashCodeCache;
        return t;*/
    }

    @Override
    public MapTerm deepClone() {
        MapTermImpl t = new MapTermImpl();
        for (Term k: this.map.keySet())
            t.map.put(k.clone(), map.get(k).clone());
        t.hashCodeCache = this.hashCodeCache;
        return t;
    }

    @Override
    public MapTerm capply(Unifier u) {
        return this; // TODO: think about this!
    }

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;
        if (t instanceof SetTerm) return this.map.equals( ((MapTermImpl)t).map );
        return false;
    }

    @Override
    public int calcHashCode() {
        return map.hashCode();
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof NumberTerm || o instanceof StringTerm || o instanceof Literal)
            return 1;
        return super.compareTo(o);
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @Override
    public Term get(Term k) {
        return map.get(k);
    }

    @Override
    public void put(Term k, Term v) {
        map.put(k,v);
    }

    @Override
    public Term remove(Term k) {
        return map.remove(k);
    }

    @Override
    public Set<Term> keys() {
        return map.keySet();
    }

    @Override
    public Collection<Term> values() {
        return map.values();
    }

    @Override
    public void clear() {
        map.clear();
    }


    public String toString() {
        StringBuilder s = new StringBuilder("{");
        String v = "";
        for (Term k: map.keySet()) {
            s.append(v+k+"->"+map.get(k));
            v = ",";
        }
        s.append('}');
        return s.toString();
    }


    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("map-term");
        String c = "";
        for (Term k: map.keySet()) {
            Element me = (Element) document.createElement("map");
            Element ke = (Element) document.createElement("key");
            Element ve = (Element) document.createElement("value");
            ke.appendChild( k.getAsDOM(document));
            ve.appendChild( map.get(k).getAsDOM(document));
            me.appendChild(ke);
            me.appendChild(ve);
            me.setAttribute("sep", c);
            c = ",";
            u.appendChild(me);
        }
        return u;
    }

    /*@Override
    public String getAsJSON(String identation) {
        StringBuilder json = new StringBuilder(identation+"{\n");
        String c = "";
        for (Term k: map.keySet()) {
            json.append(c+"  \""+k+"\" : "+ map.get(k).getAsJSON(identation+"  ") );
            c = ",\n";
        }
        json.append("\n"+identation+"}");
        return json.toString();
    }*/

    @Override
    public JsonValue getAsJson() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        for (Term k: map.keySet()) {
            json.add(k.toString(), map.get(k).getAsJson() );
        }
        return json.build();
    }
}
