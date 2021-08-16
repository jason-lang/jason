package jason.infra.local;

import jason.asSemantics.*;

/**
 * Interface for objects that want to listen sent messages in
 * Local architecture.
 *
 * @author Jomi
 */
public interface MsgListener {
    public void msgSent(Message m);
}
