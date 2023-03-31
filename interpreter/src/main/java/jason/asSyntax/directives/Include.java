package jason.asSyntax.directives;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSemantics.Agent;
import jason.asSyntax.Atom;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.asSyntax.parser.as2j;
import jason.runtime.SourcePath;
import jason.util.Config;

/** Implementation of the <code>include</code> directive. */
public class Include extends DefaultDirective implements Directive {

    static Logger logger = Logger.getLogger(Include.class.getName());

    private SourcePath aslSourcePath = new SourcePath();

    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        if (outerContent == null)
            return null;

        // handles file (arg[0])
        String file = ((StringTerm)directive.getTerm(0)).getString().replaceAll("\\\\", "/");
        try {
            file = aslSourcePath.fixPath(file);
            InputStream in = null;
            String outerPrefix = outerContent.getASLSrc(); // the source file that has the include directive
            if (outerPrefix != null) {
                // get the directory of the source of the outer agent and
                // try to find the included source in the same directory
                // or in the source paths
                SourcePath newpath = new SourcePath();
                newpath.addParentInPath(outerPrefix);
                newpath.addAll(aslSourcePath);
                file = newpath.fixPath(file);
            }
            if (file.startsWith(SourcePath.CRPrefix)) {
                // outer is loaded from a resource ("application".jar) file, used for java web start
                in = Agent.class.getResource(file.substring(SourcePath.CRPrefix.length())).openStream();
            }

            if (in == null) {
                try {
                    in = new URL(file).openStream();
                } catch (java.net.MalformedURLException e) {
                    // try as a file... just in case
                    in = new URL("file:" + file).openStream();
                }
            }

            // handles namespace (args[1])
            Atom ns = directive.getNS();
            if (directive.getArity() > 1) {
                if (directive.getTerm(1).isVar()) {
                    ns = new Atom("ns"+NameSpace.getUniqueID());
                    directive.setTerm(1, ns);
                } else {
                    if (! directive.getTerm(1).isAtom()) {
                        logger.log(Level.SEVERE, "The second parameter of the directive include (the namespace) should be an atom and not "+directive.getTerm(1)+". It is being ignored!");
                    } else {
                        ns = new Atom( ((Atom)directive.getTerm(1)).getFunctor() );
                    }
                }
            }
            Agent ag = new Agent();
            ag.initAg();
            as2j sparser = new as2j(in);
            sparser.setASLSource(file);
            ag.setASLSrc(file);
            sparser.setNS(ns);
            sparser.agent(ag);
            logger.fine("as2j: AgentSpeak program '"+file+"' parsed successfully!");
            return ag;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"The included file '"+file+"' was not found! (it is being included in the agent '"+outerContent.getTS().getAgArch().getAgName()+"')");
        } catch (jason.asSyntax.parser.ParseException e) {
            logger.log(Level.SEVERE,"as2j: error parsing \"" + file + "\": "+e.getMessage()+ "(it is being included in the agent '"+outerContent.getTS().getAgArch().getAgName()+"')");
        } catch (Exception e) {
            logger.log(Level.SEVERE,"as2j: error including \"" + file + "\"", e);
        }
        return null;
    }

    public void setSourcePath(SourcePath sp) {
        aslSourcePath = sp;
    }

    public SourcePath getSourcePaths() {
        return aslSourcePath;
    }

}
