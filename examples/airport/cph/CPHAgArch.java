package cph;

import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

import java.util.*;

public class CPHAgArch extends AgArch {

    /** overridden to ignore bid messages */
    public void checkMail() {
        super.checkMail(); // read mail into C.MB
        Queue<Message> mbox = getTS().getC().getMailBox();
        Iterator<Message> i = mbox.iterator();
        while (i.hasNext()) {
            Message im = i.next();
            if (im.getPropCont().toString().startsWith("bid")) {
                i.remove();
                if (getTS().getSettings().verbose() >= 2) {
                    System.out.println("Customised architecture of agent " + getAgName() + " is removind message " + im);
                }
            }
        }
    }

    @Override
    public void act(ActionExec action) { //, List<ActionExec> feedback) {
        // get the action to be performed
        Structure taction = action.getActionTerm();
        if (!taction.getFunctor().equals("disarm")) {
            super.act(action);
        } else {
            System.out.println("A CPH is trying to disarm a bomb! Disabled by its architecture.");
        }
    }
}
