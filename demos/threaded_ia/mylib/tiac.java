package mylib;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

/** same result as tia.java, but using ConcurrentInternalAction */

public class tiac extends ConcurrentInternalAction {

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'mylib.tiac'");

        final String key = suspendInt(ts, "tia", 5000); // suspend the intention (max 3 seconds)

        new Thread() { // to not block the agent thread, start a thread that performs the task and resume the intention latter
            public void run() {

                int arg = 0;  // gets the first argument
                try {
                    arg = (int)((NumberTerm)args[0]).solve();
                } catch (NoValueException e1) {
                    e1.printStackTrace();
                }

                VarTerm result = (VarTerm)args[1]; // gets the second argument

                // does the task...
                ts.getAg().getLogger().info("runnig tiac thread for argument "+arg+"....");

                // do a long long task here
                try {
                    sleep(arg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ts.getAg().getLogger().info("finished tiac thread....");

                // changes the Unifier with the value for the var
                un.unifies(result, ASSyntax.createNumber(arg*1.618));

                resumeInt(ts, key); // resume the intention with success
                //failInt(ts, key); // resume the intention with fail
            }
        }.start();

        // returns just after the thread creation, so to not block the agent thread
        return true;
    }

    /** called back when some intention should be resumed/failed by timeout */
    @Override
    public void timeout(TransitionSystem ts, String intentionKey) {
        ts.getAg().getLogger().info("timeout!!!!");
        // this method have to decide what to do with actions finished by timeout
        // 1: resume
        //resumeInt(ts,intentionKey);

        // 2: fail
        failInt(ts, intentionKey);
    }

}
