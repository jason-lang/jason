package screen;
import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class Counters extends JFrame {

    private static final long serialVersionUID = 1L;

    // Singleton pattern
    public static Counters c = new Counters();;
    public static Counters get() {
        return c;
    }

    JSlider[]    places = new JSlider[9];
    JLabel[]     names = new JLabel[9];

    int lastPlace = 0;

    private Counters() {
        super("Agent's counters");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        for (int i=0; i<places.length; i++) {
            places[i] = new JSlider();
            places[i].setMinimum(0);
            places[i].setMaximum(500);
            places[i].setValue(0);
            //places[i].setEnabled(false);
            names[i]  = new JLabel(" "+i);
            //names[i].setAlignmentX(JLabel.RIGHT_ALIGNMENT);

            JPanel p = new JPanel(new BorderLayout());
            //p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
            p.add(BorderLayout.EAST, names[i]);
            p.add(BorderLayout.CENTER, places[i]);
            //p.add(names[i]);
            p.add(places[i]);
            getContentPane().add(p);
        }
        pack();
        setSize(800,300);
        setVisible(true);
    }

    public synchronized int getPlace(String agName) {
        if (lastPlace < names.length) {
            names[lastPlace].setText(agName);
            return lastPlace++;
        } else {
            return -1;
        }
    }

    public void setVl(int pos, int vl) {
        places[pos].setValue(vl);
    }

    public static void main(String[] args) {
        get().getPlace("ag1");
        get().getPlace("ag2");
    }

}
