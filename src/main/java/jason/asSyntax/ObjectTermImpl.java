package jason.asSyntax;

import java.lang.reflect.Method;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ObjectTermImpl extends DefaultTerm implements ObjectTerm {
    private static final long serialVersionUID = 1L;
    
    private final Object o;
    private       Method mclone;
    private       boolean hasTestedClone = false;
    
    /** Creates a new Term Wrapper for java object */
    public ObjectTermImpl(Object o) {
        this.o = o;
    }
    
    public Object getObject() {
        return o;
    }
    
    @Override
    protected int calcHashCode() {
        return o.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this.o == null) return false;
        if (o == null) return false;
        
        if (o instanceof ObjectTermImpl) {
            return this.o.equals(((ObjectTermImpl) o).o);
        }
        
        /*if (o instanceof VarTerm) {
            Term value = ((VarTerm) o).getValue();
            if (value instanceof ObjectTermImpl) {
                return this.o.equals(((ObjectTermImpl) value).o);
            }
        }*/
        return false;        
    }
    
    @Override
    public ObjectTerm clone() {
        try {
            if (!hasTestedClone) {
                hasTestedClone = true;
                mclone = o.getClass().getMethod("clone", (Class[])null);
            }
            if (mclone != null) {
                return new ObjectTermImpl(mclone.invoke(o, (Object[])null));
            }
        } catch (Exception e) {
            //System.err.println("The object inside ObjectTerm should be clonable!");
            //e.printStackTrace();
        }
        return this;
    }

    @Override
    public String toString() {
        return o.toString();
    }
    
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("object-term");
        u.appendChild(document.createTextNode(o.toString()));
        return u;
    }
}
