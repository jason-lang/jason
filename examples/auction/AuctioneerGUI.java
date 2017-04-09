import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import jason.architecture.*;
import jason.asSemantics.ActionExec;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

import javax.swing.*;

/** example of agent architecture's functions overriding */
public class AuctioneerGUI extends AgArch {

    JTextArea jt;
    JFrame    f;
    JButton auction;

    int auctionId = 0;

    public AuctioneerGUI() {
        jt = new JTextArea(10, 30);
        auction = new JButton("Start new auction");
        auction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                auctionId++;
                Literal goal = ASSyntax.createLiteral("start_auction", ASSyntax.createNumber(auctionId));
                getTS().getC().addAchvGoal(goal, null);
                auction.setEnabled(false);
            }
        });

        f = new JFrame("Auctioneer agent");
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(BorderLayout.CENTER, new JScrollPane(jt));
        f.getContentPane().add(BorderLayout.SOUTH, auction);
        f.pack();
        f.setVisible(true);
    }

    @Override
    public void act(ActionExec action) { //, List<ActionExec> feedback) {
        if (action.getActionTerm().getFunctor().startsWith("show_winner")) {
            jt.append("Winner of auction  " + action.getActionTerm().getTerm(0));
            jt.append(" is " + action.getActionTerm().getTerm(1) + "\n");
            action.setResult(true);
            actionExecuted(action);

            auction.setEnabled(true); // enable GUI button
        } else {
            super.act(action); // send the action to the environment to be performed.
        }
    }

    @Override
    public void stop() {
        f.dispose();
        super.stop();
    }
}
