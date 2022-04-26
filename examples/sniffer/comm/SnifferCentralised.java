package comm;

import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.*;
import jason.infra.local.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Customisation of an agent architecture to sniff the MAS running locally
 *
 * @author Jomi
 */
public class SnifferCentralised extends AgArch implements MsgListener {

    @Override
    public void init() {
        if (RunLocalMAS.getRunner() != null) // is it the case of Local platform being used
            LocalAgArch.addMsgListener(this);
    }

    // method called-back when some message is exchanged
    public void msgSent(Message m) {
        //getTS().getLogger().fine("Message:"+m);

        // add a belief in the agent mind
        // format: msgSent(time(YY,MM,DD,HH,MM,SS),id,irt,ilf,sender,receiver,content)

        Calendar now = new GregorianCalendar();
        Structure p = ASSyntax.createStructure("time",
                                               ASSyntax.createNumber(now.get(Calendar.YEAR)),
                                               ASSyntax.createNumber(now.get(Calendar.MONTH)),
                                               ASSyntax.createNumber(now.get(Calendar.DAY_OF_MONTH)),
                                               ASSyntax.createNumber(now.get(Calendar.HOUR)),
                                               ASSyntax.createNumber(now.get(Calendar.MINUTE)),
                                               ASSyntax.createNumber(now.get(Calendar.SECOND)));
        Literal e = ASSyntax.createLiteral("msg_sent", p);

        e.addTerm(new StringTermImpl(m.getMsgId()));
        if (m.getInReplyTo() == null) {
            e.addTerm(new Atom("nirt"));
        } else {
            e.addTerm(new StringTermImpl(m.getInReplyTo()));
        }
        e.addTerm(new Atom(m.getIlForce()));
        e.addTerm(new Atom(m.getSender()));
        e.addTerm(new Atom(m.getReceiver()));
        e.addTerm(new StringTermImpl(m.getPropCont().toString()));
        try {
            getTS().getAg().addBel(e);
        } catch (RevisionFailedException e1) {
            e1.printStackTrace();
        }
    }
}
