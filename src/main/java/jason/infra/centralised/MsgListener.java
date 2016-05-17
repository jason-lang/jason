package jason.infra.centralised;

import jason.asSemantics.*;

/**
 * Interface for objects that want to listen sent messages in
 * centralised architecture.
 * 
 * @author Jomi
 */
public interface MsgListener {
    public void msgSent(Message m);
}
