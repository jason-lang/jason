import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import cartago.INTERNAL_OPERATION;
import cartago.ObsProperty;
import cartago.tools.GUIArtifact;

public class Calendar extends GUIArtifact {
    Term[] days = { 
                ASSyntax.createAtom("sunday"),
                ASSyntax.createAtom("monday"),
                ASSyntax.createAtom("tuesday"),
                ASSyntax.createAtom("wednesday"),
                ASSyntax.createAtom("thursday"),
                ASSyntax.createAtom("friday"),
                ASSyntax.createAtom("saturday")
            };
    
    
    public void setup() {
        defineObsProperty("today", days[0]);
        initGUI();
    }

    JSlider s = new JSlider();
    
    void initGUI() {
        JFrame f = new JFrame("Calendar");
        
        s.setMinimum(0);
        s.setMaximum(6);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setValue(0);
        Dictionary<Integer, JLabel> lbs = new Hashtable<Integer, JLabel>();
        lbs.put(0, new JLabel("S"));
        lbs.put(1, new JLabel("M"));
        lbs.put(2, new JLabel("T"));
        lbs.put(3, new JLabel("W"));
        lbs.put(4, new JLabel("T"));
        lbs.put(5, new JLabel("F"));
        lbs.put(6, new JLabel("S"));
        s.setLabelTable(lbs);
        linkChangeEventToOp(s, "updateDay");
        
        f.add(s);
        f.pack();
        f.setVisible(true);
    }
    
    @INTERNAL_OPERATION void updateDay(ChangeEvent ev) {
        try {
            ObsProperty prop = getObsProperty("today");
            prop.updateValue(days[ (int)s.getValue() ]);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}

