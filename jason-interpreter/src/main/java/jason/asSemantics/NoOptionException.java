package jason.asSemantics;

import jason.JasonException;
import jason.asSyntax.*;

/** no option from selectOption function */
public class NoOptionException extends JasonException {

    /**
     * Constructs an instance of <code>JasonException</code> with the specified detail message
     * and error description term.
     *
     * @param msg the detail message.
     * @param error the term that details (in AgentSpeak) the error
     */
    public NoOptionException(String msg, Term error) {
        super(msg, error);
    }

}
