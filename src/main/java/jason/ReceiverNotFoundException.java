package jason;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

public class ReceiverNotFoundException extends JasonException {

    private static final long serialVersionUID = 1L;

    public static final Term RNF   = new Atom("receiver_not_found");

    public ReceiverNotFoundException() {
    }

    public ReceiverNotFoundException(String msg) {
        super(msg, RNF);
    }

    public ReceiverNotFoundException(String msg, String agNotFound) {
        super(msg, RNF);
        addErrorAnnot(ASSyntax.createStructure("agent_not_found", new Atom(agNotFound)));
    }

    public ReceiverNotFoundException(String msg, Exception cause) {
        super(msg, RNF);
        initCause(cause);
    }

}
