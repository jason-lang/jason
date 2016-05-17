package jason.util;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;



/** 
 * Convert an agent asl code to LaTeX. 
 *
 * @author Jomi
 */
public class asl2tex extends asl2xml {

    String style = "/xml/asl2tex.xsl";
    
    public asl2tex() {}
    public asl2tex(String style) {
        this.style = style;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("The asl code file must be informed");
            System.exit(1);
        }
        new asl2tex().run(args[0]);
    }
    
    public String transform(Document agState)  throws Exception {
        StringWriter so = new StringWriter();
        getTransformer().transform(new DOMSource(agState), new StreamResult(so));
        return so.toString().replace("_", "\\_");
    }
  

    public Transformer getTransformer()  throws Exception {
        if (transCache == null) {
            transCache = getFactory().newTransformer(
                    new StreamSource(asl2tex.class.getResource(style).openStream()));
        }
        return transCache;
    }
}
