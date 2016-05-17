package jason.util;

import java.util.HashSet;
import java.util.Set;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBody.BodyType;
import jason.asSyntax.Trigger.TEType;



/** 
 * Convert an agent asl code to dot (http://www.graphviz.org/) 
 * -- used to produce the graph of goals.
 *
 * @author Jomi
 */
public class asl2dot extends asl2xml {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("The asl code file must be informed");
            System.exit(1);
        }
        new asl2dot().run(args[0]);
    }

    public String transform(Agent ag)  throws Exception {
        StringBuilder so = new StringBuilder();
        so.append("// dot file used to generate goals graph\n");
        so.append("// run: dot -Tpdf <theoutput> -o goals.pdf\n");
        so.append("digraph goals {\n");
        so.append("    rankdir=BT;\n\n");
        
        Set<String> done = new HashSet<String>();
        for (Plan p: ag.getPL()) {
            if (p.getTrigger().getType() == TEType.achieve) {
                String ps = p.getTrigger().getLiteral().getFunctor();
                if (!done.contains(ps)) {
                    done.add(ps);
                    so.append("    "+ps+";\n");
                }
                PlanBody b = p.getBody();
                while (b != null) {
                    if (b.getBodyType() == BodyType.achieve || b.getBodyType() == BodyType.achieveNF) {
                        String bs = ((Literal)b.getBodyTerm()).getFunctor();
                        String e = bs+ps; 
                        if (!done.contains(e)) {
                            done.add(e);
                            so.append("    "+bs+" -> "+ps+";\n");
                        }
                    }
                    b = b.getBodyNext();
                }
            }
        }
        
        so.append("}\n");
        return so.toString();
    }

}
