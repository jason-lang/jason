package jason.architecture;

import java.util.Collection;

import jason.asSemantics.ActionExec;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.runtime.RuntimeServicesInfraTier;



/**
 * This interface is implemented by the infrastructure tier (Saci/Jade/Centralised/...)
 * to provide concrete perception, action, and communication to the agent architecture.
 **/
public interface AgArchInfraTier {

    /** Gets the agent's perception as a set of Literals */
    public Collection<Literal> perceive();

    /** Reads the agent's mailbox and adds messages into the agent's circumstance */
    public void checkMail();

    /**
     * Executes the action <i>action</i> in the environment
     */
    public void act(ActionExec action);

    /** Returns true whether the agent can sleep according to the arch */
    public boolean canSleep();

    /** Gets the agent's name */
    public String getAgName();

    /** Sends a Jason message in a specific infrastructure */
    public void sendMsg(Message m) throws Exception;

    /** Broadcasts a Jason message in a specific infrastructure */
    public void broadcast(Message m) throws Exception;

    /** Checks whether the agent is running (alive) */
    public boolean isRunning();

    /** Stops the agent */
    //public void stopAg();

    /** Put the agent in "sleep" mode */
    //public void sleep();

    /** Removes the agent from the "sleep" mode */
    public void wake();

    /** Gets an object with infrastructure runtime services */
    public RuntimeServicesInfraTier getRuntimeServices();
}
