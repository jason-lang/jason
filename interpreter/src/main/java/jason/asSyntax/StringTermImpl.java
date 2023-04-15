package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.Serial;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Immutable class for string terms.
 *
 * @author Jomi
 */
public final class StringTermImpl extends DefaultTerm implements StringTerm {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(StringTermImpl.class.getName());

    private final String value;

    public StringTermImpl() {
        super();
        value = null;
    }

    public StringTermImpl(String fs) {
        value = fs;
    }

    public StringTermImpl(StringTermImpl t) {
        value   = t.getString();
        srcInfo = t.srcInfo;
    }

    public String getString() {
        return value;
    }

    public StringTerm clone() {
        return this;
    }

    public static StringTerm parseString(String sTerm) {
        as2j parser = new as2j(new StringReader(sTerm));
        try {
            return (StringTerm)parser.term();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing string term " + sTerm,e);
            return null;
        }
    }

    @Override
    public boolean isString() {
        return true;
    }

    public int length() {
        if (value == null)
            return 0;
        else
            return value.length();
    }

    @Override
    public boolean equals(Object t) {
        if (t == this) return true;

        if (t instanceof StringTerm st) {
            if (value == null)
                return st.getString() == null;
            else
                return value.equals(st.getString());
        }
        return false;
    }

    @Override
    protected int calcHashCode() {
        if (value == null)
            return 0;
        else
            return value.hashCode();
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof VarTerm)
            return o.compareTo(this) * -1;
        if (o instanceof NumberTerm)
            return 1;
        return super.compareTo(o);
    }


    public String toString() {
        return "\""+value+"\"";
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = document.createElement("string-term");
        u.appendChild(document.createTextNode(toString()));
        return u;
    }

    @Override
    public JsonValue getAsJson() {
        return Json.createValue( value );
    }
}
