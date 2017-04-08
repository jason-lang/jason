package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSyntax.Atom;
import jason.asSyntax.Pred;
import jason.asSyntax.parser.as2j;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implementation of the <code>namespace</code> directive. */
public class NameSpace implements Directive {

    public static final String LOCAL_PREFIX = "#";

    static Logger logger = Logger.getLogger(NameSpace.class.getName());

    private Map<Atom,Atom> localNSs = new HashMap<Atom,Atom>();

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        return innerContent;
    }

    Stack<Atom> oldNS = new Stack<Atom>();

    @Override
    public void begin(Pred directive, as2j parser) {
        if (! directive.getTerm(0).isAtom()) {
            logger.log(Level.SEVERE, "The first parameter of the directive namespace should be an atom and not "+directive.getTerm(0));
            return;
        }
        Atom ns = new Atom( ((Atom)directive.getTerm(0)).getFunctor() );

        if (directive.getArity() > 1) {
            if (! directive.getTerm(1).isAtom()) {
                logger.log(Level.SEVERE, "The second parameter of the directive namespace should be an atom and not "+directive.getTerm(1));
                return;
            }
            String type = ((Atom)directive.getTerm(1)).getFunctor();
            if (!type.equals("local") && !type.equals("global")) {
                logger.log(Level.SEVERE, "The second parameter of the directive namespace should be either local or global");
                return;
            }
            if (type.equals("global") && isLocalNS(ns)) {
                logger.warning("The namespace "+ns+" was previously defined as local, changing it to globall!");
                localNSs.remove(ns);
            }
            if (type.equals("local")) {
                ns = addLocalNS(ns);
            }
        }

        oldNS.push(parser.getNS());
        parser.setNS(ns);
    }

    @Override
    public void end(Pred directive, as2j parser) {
        if (!oldNS.isEmpty())
            parser.setNS(oldNS.pop());
    }

    public boolean isLocalNS(Atom ns) {
        return localNSs.get(ns) != null;
    }

    public Atom map(Atom ns) {
        Atom n = localNSs.get(ns);
        if (n == null)
            return ns;
        else
            return n;
    }

    static private AtomicInteger nsCounter = new AtomicInteger(0);
    public static int getUniqueID() {
        return nsCounter.incrementAndGet();
    }

    private synchronized Atom addLocalNS(Atom ns) {
        Atom newNS = localNSs.get(ns);
        if (newNS == null) {
            newNS = new Atom(LOCAL_PREFIX+nsCounter.incrementAndGet()+ns);
            localNSs.put(ns,newNS);
        }
        return newNS;
    }

}
