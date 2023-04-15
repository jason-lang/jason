package jason.mas2j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jason.asSyntax.Structure;
import jason.asSyntax.Term;

/**
 * Used to store class parameters in .mas2j file, e.g.
 *      environment: Mars(a,b,c);
 * this class stores
 *   className  = Mars,
 *   parameters = {a,b,c}
 *
 * @author jomi
 */
public class ClassParameters implements Serializable {
    private static final long serialVersionUID = -2202523371417849466L;

    private String className;
    private List<String> parameters = new ArrayList<>();
    private String host;

    public ClassParameters() {}
    public ClassParameters(String className) {
        this.className = className;
    }
    public ClassParameters(Structure s) {
        className = s.getFunctor();
        if (s.getArity() > 0) {
            for (Term t: s.getTerms()) {
                parameters.add(t.toString());
            }
        }
    }

    public ClassParameters copy() {
        ClassParameters newcp = new ClassParameters(this.className);
        newcp.parameters = new ArrayList<>(this.parameters);
        newcp.host = this.host;
        return newcp;
    }

    public void setClassName(String cn) {
        className = cn;
    }
    public String getClassName() {
        return className;
    }


    public void addParameter(String s) {
        parameters.add(s);
    }
    public Collection<String> getParameters() {
        return parameters;
    }
    public String getParameter(int index) {
        if (parameters.size() > index)
            return parameters.get(index);
        else
            return null;
    }
    public String getParameter(String startWith) {
        for (String s: parameters)
            if (s.startsWith(startWith))
                return s;
        return null;
    }
    public boolean hasParameter(String s) {
        return parameters.contains(s);
    }
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
    public String[] getParametersArray() {
        String[] p = new String[parameters.size()];
        int i=0;
        for (String s: parameters) {
            p[i++] = removeQuotes(s);
        }
        return p;
    }

    public Object[] getTypedParametersArray() {
        Object[] p = new Object[parameters.size()];
        int i=0;
        for (String s: parameters) {
            s = removeQuotes(s);
            try {
                p[i] = Integer.parseInt(s);
            } catch (Exception e) {
                try {
                    p[i] = Double.parseDouble(s);
                } catch (Exception e3) {
                    if (s.equals("true"))
                        p[i] = true;
                    else if (s.equals("false"))
                        p[i] = false;
                    else
                        p[i] = s;
                }
            }

            i++;
        }
        return p;
    }


    /** returns parameters with space as separator */
    public String getParametersStr(String sep) {
        StringBuilder out = new StringBuilder();
        if (parameters.size() > 0) {
            Iterator<String> i = parameters.iterator();
            while (i.hasNext()) {
                out.append(i.next());
                if (i.hasNext()) out.append(sep);
            }
        }
        return out.toString();

    }

    public void setHost(String h) {
        if (h.startsWith("\""))
            host = h.substring(1,h.length()-1);
        else
            host = h;
    }
    public String getHost() {
        return host;
    }

    public String toString() {
        StringBuilder out = new StringBuilder(className);
        if (parameters.size() > 0) {
            out.append("(");
            Iterator<String> i = parameters.iterator();
            while (i.hasNext()) {
                out.append(i.next());
                if (i.hasNext()) {
                    out.append(",");
                }
            }
            out.append(")");
        }
        return out.toString();
    }

    String removeQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    @Override
    public int hashCode() {
    	return className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj == null) return false;
    	if (obj == this) return true;
    	if (! (obj instanceof ClassParameters)) return false;
    	ClassParameters o = (ClassParameters)obj;
    	if (!this.className.equals(o.className)) return false;
    	return true;
    }
}
