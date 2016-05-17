package env;

import java.awt.*;
import javax.swing.*;


public class PlanetGUI extends JPanel {

    private PlanetCell[][] planet;
    private int cellWidth, cellHeight;
    private int[] agent1;
    private int[] agent2;
    private int[] agent3;   
    private planetEnv environment;  
    private int gridSize;
    private int middle;

    public PlanetGUI(planetEnv env) {
        
        environment = env;
        update();   
        cellWidth = 20;
        cellHeight = 20;        
        setSize(600, 600);
        gridSize = 30;
        middle = gridSize/2;
    }
    
    public void paintComponent(Graphics g) {
        
        for(int row = 0; row<gridSize; row++) {
            for(int col = 0; col<gridSize; col++) {
            
                g.setColor(new Color(185, 58, 17));
                g.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.gray);
                g.drawRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                
                if(planet[col][row] != null) {
                                
                    if(col == middle && row == middle) {
                        g.setColor(Color.white);    
                        g.fillRect((col * cellWidth) +3, (row * cellHeight) +3, cellWidth - 5, cellHeight - 5);
                    }
                    else {
                        Resource r = (Resource) planet[col][row];
                        int type = r.getType();
                        int amount = r.getAmount();
                        switch(type) {
                            case 1:
                                g.setColor(Color.orange);   
                                break;
                            case 2:
                                g.setColor(Color.green);
                                break;
                            case 3:
                                g.setColor(Color.blue);
                                break;
                        }
                        
                        g.fillOval(col*cellWidth +3, row*cellHeight +3, cellWidth - 5, cellHeight - 5);
                        g.setColor(Color.black);
                        g.setFont(new Font("Arial", Font.PLAIN, 11));
                        g.drawString(""+amount, (col * cellWidth) +8, (row * cellHeight) +14);
                    }

                }

            }
        }
        
        g.setColor(Color.black);
        g.fillRoundRect(agent1[0]*cellWidth +3, agent1[1]*cellHeight +3, cellWidth - 5, cellHeight - 5, 3, 3);
        g.setColor(Color.white);
        g.drawString("A", (agent1[0]*cellWidth) +7, (agent1[1]*cellHeight) +14);

        g.setColor(Color.black);
        g.fillRoundRect(agent2[0]*cellWidth +3, agent2[1]*cellHeight +3, cellWidth - 5, cellHeight - 5, 3, 3);
        g.setColor(Color.white);
        g.drawString("B", (agent2[0]*cellWidth) +7, (agent2[1]*cellHeight) +14);

        g.setColor(Color.black);
        g.fillRoundRect(agent3[0]*cellWidth +3, agent3[1]*cellHeight +3, cellWidth - 5, cellHeight - 5, 3, 3);
        g.setColor(Color.white);
        g.drawString("C", (agent3[0]*cellWidth) +7, (agent3[1]*cellHeight) +14);

    }


    public void update() {
        
        planet = environment.getPlanet();
        agent1 = environment.geta1();
        agent2 = environment.geta2();
        agent3 = environment.geta3();
        repaint();
    }
    
    public Dimension getPreferredSize() {
        
    return new Dimension(600, 600); 
           
   }
    
    public static void main(String[] args) {
    
    }
}
