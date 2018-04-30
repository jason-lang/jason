package jason.asSyntax.directives;

import java.io.File;
import java.io.FileInputStream;
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
            InputStream in = null;
            // test include from jar
            if (file.startsWith("$")) { // the case of "$jasonJar/src/a.asl"
                String jar = file.substring(1,file.indexOf("/"));
                if (Config.get().get(jar) == null) {
                    logger.log(Level.SEVERE,"The included file '"+jar+"' is not configured");
                    return null;
                }
                String path = Config.get().get(jar).toString();
                file = "jar:file:" + path + "!" + file.substring(file.indexOf("/"));
                in = new URL(file).openStream();
            } else {
                String outerPrefix = outerContent.getASLSrc(); // the source file that has the include directive
                if (outerContent != null && outerPrefix != null) {
                    // check if the outer is URL
                    if (outerPrefix.startsWith("jar")) {
                        outerPrefix = outerPrefix.substring(0,outerPrefix.indexOf("!")+1) + "/";
                        file = aslSourcePath.fixPath(file, outerPrefix);
                        in = new URL(file).openStream();
                    } else if (outerPrefix.startsWith(SourcePath.CRPrefix)) {
                        // outer is loaded from a resource ("application".jar) file, used for java web start
                        int posSlash = outerPrefix.lastIndexOf("/");

                        SourcePath newpath = new SourcePath();
                        if (outerPrefix.indexOf("/") != posSlash) { // has only one slash
                            newpath.addPath(outerPrefix.substring(SourcePath.CRPrefix.length()+1,posSlash));
                        }
                        newpath.addAll(aslSourcePath);

                        file = newpath.fixPath(file, SourcePath.CRPrefix+"/");
                        in = Agent.class.getResource(file.substring(SourcePath.CRPrefix.length())).openStream();
                    } else if (outerPrefix.startsWith("file:") || outerPrefix.startsWith("http:") || outerPrefix.startsWith("https:")) {
                        URL url = new URL(new URL(outerPrefix), file);
                        file = url.toString();
                        in = url.openStream();
                    } else if (file.startsWith("jar:") || file.startsWith("file:") || file.startsWith("http:") || file.startsWith("https:")) {
                        URL url = new URL(file);
                        file = url.toString();
                        in = url.openStream();
                    } else {
                        // get the directory of the source of the outer agent and
                        // try to find the included source in the same directory
                        // or in the source paths
                        SourcePath newpath = new SourcePath();
                        newpath.addPath(new File(outerPrefix).getAbsoluteFile().getParent());
                        newpath.addAll(aslSourcePath);
                        file = newpath.fixPath(file, null);
                        in = new FileInputStream(file);
                    }
                } else {
                    in = new FileInputStream(aslSourcePath.fixPath(file, null));
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
            ag.setASLSrc(file);
            as2j sparser = new as2j(in);
            sparser.setNS(ns);
            sparser.agent(ag);
            logger.fine("as2j: AgentSpeak program '"+file+"' parsed successfully!");
            return ag;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"The included file '"+file+"' was not found! (it is being included in the agent '"+outerContent.getASLSrc()+"')");
        } catch (Exception e) {
            logger.log(Level.SEVERE,"as2j: error parsing \"" + file + "\"", e);
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
