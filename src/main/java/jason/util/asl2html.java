package jason.util;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;



/** 
 * Convert an agent asl code to HTML. 
 *
 * @author Jomi
 */
public class asl2html extends asl2xml {

    String style = "/xml/asl2html.xsl";
    
    public asl2html() {}
    public asl2html(String style) {
        this.style = style;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("The asl code file must be informed");
            System.exit(1);
        }
        new asl2html().run(args[0]);
    }

    public Transformer getTransformer()  throws Exception {
        if (transCache == null) {
            transCache = getFactory().newTransformer(
                    new StreamSource(asl2html.class.getResource(style).openStream()));
        }
        return transCache;
    }
}
