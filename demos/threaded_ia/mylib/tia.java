package mylib;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class tia extends DefaultInternalAction {

    // the intention is suspended by the execution of this internal action
    @Override
    public boolean suspendIntention() {
        return true;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'mylib.tia'");

        Circumstance C = ts.getC();

        // suspends the intention
        Intention i = C.getSelectedIntention(); // the intention running this internal action
        i.setSuspended(true);
        final String pendingId = "suspended by tia "+i.getId(); // creates and unique id for this suspension (e.g. to be seens in mind inspector)
        C.addPendingIntention(pendingId, i);

        // creates a thread for a task
        new Thread() {
            @Override
            synchronized public void run() {
                ts.getAg().getLogger().info("runnig tia thread....");

                // do a long long task here
                try {
                    wait(3000);
                } catch (InterruptedException e) { e.printStackTrace(); }
                ts.getAg().getLogger().info("finished tia thread....");

                // resumes the intention
                if (i.isSuspended() && C.removePendingIntention(pendingId) != null) {  // if still suspended
                    i.setSuspended(false);
                    i.peek().removeCurrentStep(); // removes the tia call in the plan
                    C.resumeIntention(i); // puts the intention back to the set of active intentions
                }
            }
        }.start();

        // returns just after the thread creation, so to not block the agent thread
        return true;
    }
}
