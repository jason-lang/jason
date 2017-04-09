package jason;


public class ReceiverNotFoundException extends java.lang.Exception {

    private static final long serialVersionUID = 1L;

    public ReceiverNotFoundException() {
    }

    public ReceiverNotFoundException(String msg) {
        super(msg);
    }

    public ReceiverNotFoundException(String msg, Exception cause) {
        super(msg);
        initCause(cause);
    }

}
