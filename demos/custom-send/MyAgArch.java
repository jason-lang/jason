import java.util.*;

import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class MyAgArch extends AgArch {

    @Override
    public void init() throws Exception {
        super.init();

        // relace the .send implementation by MySend
        getTS().getAg().setIA("jason.stdlib.send", new MySend());

        // creates a thread that simulates the receive of an ack
        new Thread() {
            public void run() {
                while (isRunning()) {
                    try {
                        Thread.sleep(3000);

                        System.out.println("simulates the ack comming .... ");
                        // retrieve the pending intention with wack id
                        for (String k: getTS().getC().getPendingIntentions().keySet()) {
                            if (k.startsWith("wack")) {
                                Intention intention = getTS().getC().removePendingIntention(k);
                                intention.peek().removeCurrentStep();
                                getTS().getC().resumeIntention(intention, null);
                                wakeUpAct();
                                System.out.println("done!");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void sendMsg(Message m) {
        System.out.println("\nSending msg "+m+"...");

        // add the current intention in pending intentions
        getTS().getC().addPendingIntention("wack-"+m.getMsgId(), getTS().getC().getSelectedIntention());
    }
}
