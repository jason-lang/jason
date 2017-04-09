package gui;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

/** internal action that creates a simple GUI with two buttons that trigger AS plans */
public class create extends DefaultInternalAction {

    int runCount = 0;

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        // get the window title
        String title = ((StringTerm)args[0]).getString();

        // create the windows
        final JButton run = new JButton("run");
        final JButton stop = new JButton("stop");
        stop.setEnabled(false);
        JPanel buttons = new JPanel();
        buttons.add(new JLabel("Use the following buttons to control the agent intentions: "));
        buttons.add(run);
        buttons.add(stop);

        JFrame frame = new JFrame(title);
        frame.getContentPane().add(buttons);
        frame.pack();
        frame.setVisible(true);

        // add the event listeners
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // creates a new event +!run so that the agent can react to the button
                runCount++;
                ts.getC().addAchvGoal(Literal.parseLiteral("run("+runCount+")"), null);
                stop.setEnabled(true);
                run.setEnabled(false);
            }
        });
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ts.getC().addAchvGoal(Literal.parseLiteral("stop("+runCount+")"), null);
                stop.setEnabled(false);
                run.setEnabled(true);
            }
        });

        return true;
    }
}
