package jason.asSemantics;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;


public class Message implements Serializable {

    private String ilForce  = null;
    private String sender   = null;
    private String receiver = null;
    private Object propCont = null;
    private String msgId    = null;
    private String inReplyTo = null;

    private static AtomicInteger idCount = new AtomicInteger(0);

    public final static String[] knownPerformatives = {"tell","untell","achieve","unachieve","askOne","askAll","tellHow", "untellHow","askHow"};

    public final static String msgIdPrefix        = "mid";
    public final static String msgIdSyncAskPrefix = "samid";

    public final static String kqmlReceivedFunctor = "kqml_received";
    public final static String kqmlDefaultPlans    = "$jasonJar/asl/kqmlPlans.asl";

    public Message() {
    }

    public Message(String ilf, String s, String r, Object c) {
        this(ilf, s, r, c, msgIdPrefix+(idCount.incrementAndGet()));
    }

    public Message(String ilf, String s, String r, Object c, String id) {
        setIlForce(ilf);
        sender   = s;
        receiver = r;
        propCont = c;
        msgId    = id;
    }

    public Message(Message m) {
        ilForce  = m.ilForce;
        sender   = m.sender;
        receiver = m.receiver;
        propCont = m.propCont;
        msgId    = m.msgId;
        inReplyTo= m.inReplyTo;
    }

    public void setSyncAskMsgId() {
        msgId = msgIdSyncAskPrefix+(idCount.incrementAndGet());
    }

    public String getIlForce() {
        return ilForce;
    }

    public void setIlForce(String ilf) {
        if (ilf.equals("ask-one")) ilf = "askOne";
        if (ilf.equals("ask-all")) ilf = "askAll";
        if (ilf.equals("tell-how")) ilf = "tellHow";
        if (ilf.equals("ask-how")) ilf = "askHow";
        if (ilf.equals("untell-how")) ilf = "untellHow";
        ilForce = ilf;
    }

    public boolean isAsk() {
        return ilForce.startsWith("ask");
    }
    public boolean isTell() {
        return ilForce.startsWith("tell");
    }
    public boolean isUnTell() {
        return ilForce.startsWith("untell");
    }

    public boolean isReplyToSyncAsk() {
        return inReplyTo != null && inReplyTo.startsWith(msgIdSyncAskPrefix);
    }

    public boolean isKnownPerformative() {
        for (String s: knownPerformatives) {
            if (ilForce.equals(s))
                return true;
        }
        return false;
    }

    public void setPropCont(Object o) {
        propCont = o;
    }
    public Object getPropCont() {
        return propCont;
    }

    public String getReceiver() {
        return receiver;
    }
    public void setSender(String agName) {
        sender = agName;
    }
    public String getSender() {
        return sender;
    }
    public void setReceiver(String agName) {
        receiver = agName;
    }

    public String getMsgId() {
        return msgId;
    }
    public void setMsgId(String id) {
        msgId = id;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }
    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public Message clone() {
        return new Message(this);
    }

    /**
     * Creates a new message object based on a string that
     * follows the format of the toString of Message class.
     *
     * @author Rogier
     * @param msg the string message
     * @return the parsed Message
     */
    public static Message parseMsg(String msg) throws ParseException {
        int one, two;
        Message newmsg = new Message();
        if (msg.startsWith("<")) {
            one = msg.indexOf(",");
            int arrowIndex = msg.indexOf("->");
            if ((arrowIndex > 0) && (one > arrowIndex)) { // If there is an arrow before the first comma
                newmsg.msgId = msg.substring(1, arrowIndex);
                newmsg.inReplyTo = msg.substring(arrowIndex + 2, one);
            } else { // If not (either there is no arrow, or there is one behind the first comma)
                newmsg.msgId = msg.substring(1, one);
            }
            two = msg.indexOf(",", one + 1);
            newmsg.sender = msg.substring(one + 1, two);
            one = msg.indexOf(",", two + 1);
            newmsg.ilForce = msg.substring(two + 1, one);
            two = msg.indexOf(",", one + 1);
            newmsg.receiver = msg.substring(one + 1, two);
            one = msg.indexOf(">", two + 1);
            String content = msg.substring(two + 1, one);
            newmsg.propCont = ASSyntax.parseTerm(content);
        }
        return newmsg;
    }

    public String toString() {
        String irt = (inReplyTo == null ? "" : "->"+inReplyTo);
        return "<"+msgId+irt+","+sender+","+ilForce+","+receiver+","+propCont+">";
    }
}
