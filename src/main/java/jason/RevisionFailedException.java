package jason;

public class RevisionFailedException extends JasonException {

    public RevisionFailedException() {
    }

    public RevisionFailedException(String msg) {
        super(msg);
    }

    public RevisionFailedException(String msg, Exception cause) {
        super(msg);
        initCause(cause);
    }
}
