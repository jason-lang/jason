package jason.util;

import jason.asSemantics.Agent;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;



/**
 * Convert an agent asl code to xml.
 *
 * @author Jomi
 */
public class asl2xml  {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("The asl code file must be informed");
            System.exit(1);
        }
        new asl2xml().run(args[0]);
    }

    void run(String file) throws Exception {
        Agent ag = loadAg(file);
        if (ag != null) {
            System.out.println(transform(ag));
        }
    }

    Agent loadAg(String file)  throws Exception {
        Agent ag = new Agent();
        ag.initAg();
        if (ag.parseAS(new File(file))) {
            ag.setASLSrc(file);
            ag.addInitialBelsInBB();
            return ag;
        } else {
            return null;
        }
    }

    public String transform(String agCode) throws Exception {
        jason.asSyntax.parser.as2j parser = new jason.asSyntax.parser.as2j(new StringReader(agCode));
        Agent ag = new Agent();
        ag.initAg();
        parser.agent(ag);
        return transform(ag.getAgProgram());
    }

    public String transform(Agent ag)  throws Exception {
        return transform(ag.getAgProgram());
    }

    public String transform(Document agState)  throws Exception {
        try {
            StringWriter so = new StringWriter();
            getTransformer().transform(new DOMSource(agState), new StreamResult(so));
            return so.toString();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    protected Transformer transCache = null;
    public Transformer getTransformer()  throws Exception {
        if (transCache == null) {
            transCache = getFactory().newTransformer();
            transCache.setOutputProperty(OutputKeys.INDENT, "yes");
        }
        return transCache;
    }

    public void setParameter(String name, Object value) {
        if (transCache != null)
            transCache.setParameter(name, value);
    }

    TransformerFactory fac = null;
    TransformerFactory getFactory() throws Exception {
        if (fac == null) {
            fac = TransformerFactory.newInstance();
            fac.setURIResolver(new URIResolver() {
                public Source resolve(String href, String base) throws TransformerException {
                    try {
                        return new StreamSource(asl2xml.class.getResource("/xml/" + href).openStream());
                    } catch (Exception e) {
                        System.err.println("Error - " + href + "-" + base);
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        }
        return fac;
    }

}
