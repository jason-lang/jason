package env;

import java.awt.*;
import javax.swing.*;

public class GUI extends JFrame {

    private planetEnv environment;
    private PlanetGUI planet;
    private JTextArea output;
    private JScrollPane outScroll;
    private Box box;
    private JLabel label;
    
    public GUI(planetEnv env) {
        super("Multi Agent System - Resource Collection");
        environment = env;
        planet = new PlanetGUI(environment);
        initialise();
    }

    public void update() {
    
        planet.update();    
    
    }

    public void initialise() {
        
        Container c = getContentPane();     
    
        label = new JLabel("Output");

        output = new JTextArea(6, 18); 
        output.setEditable(false);
        
        outScroll = new JScrollPane();
        outScroll.setViewportView(output);
        outScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        box = new Box(BoxLayout.Y_AXIS);
        box.add(planet);
        box.add(label);
        box.add(outScroll);
        
        c.add(box);
        setSize(610, 750);
        setVisible(true);
    }
    
    public void out(String out) {
        
        output.append(out + "\n");
        output.setCaretPosition(output.getDocument().getLength());
        
    }

}

