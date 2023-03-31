package jason;


public class NoValueException extends JasonException {

    private static final long serialVersionUID = 1L;

    public NoValueException() {
    }

    public NoValueException(String msg) {
        super(msg);
    }

    public NoValueException(String msg, Exception cause) {
        super(msg);
        initCause(cause);
    }

}
