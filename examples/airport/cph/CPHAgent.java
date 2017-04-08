package cph;

import jason.asSemantics.Agent;
import jason.asSemantics.Message;

public class CPHAgent extends Agent {

    /** only accepts "achieve" messages from mds robots */
    public boolean socAcc(Message m) {
        if (m.getIlForce().equals("achieve") && m.getSender().startsWith("mds")) {
            return true;
        } else {
            System.out.println("CustomAgentClass: CPH agent is not allowed to handle message "+m);
            return false;
        }
    }

}
