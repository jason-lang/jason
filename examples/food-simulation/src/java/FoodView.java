import jason.environment.grid.GridWorldView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

    
/** class that implements the View of the Game of Life application */
public class FoodView extends GridWorldView {

    private static final long serialVersionUID = 1L;

    FoodModel hmodel;

    DefaultXYDataset dataset = new DefaultXYDataset();
    
    public FoodView(FoodModel model, final FoodEnvironment env) {
        super(model, "Normative Simulation", 500);
        hmodel = model;
        setVisible(true);
        repaint();
        
        getCanvas().addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSizeW;
                int lin = e.getY() / cellSizeH;
                if (col >= 0 && lin >= 0 && col < getModel().getWidth() && lin < getModel().getHeight()) {
                    hmodel.add(FoodModel.FOOD, col, lin);
                    //env.updateNeighbors(hmodel.getAgId(col,lin));
                    update(col, lin);
                }
            }
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });
        
        JFreeChart xyc = ChartFactory.createXYLineChart( 
                "Agents' strength",
                "step",
                "strength",
                dataset, // dataset, 
                PlotOrientation.VERTICAL, // orientation, 
                true, // legend, 
                true, // tooltips, 
                true); //urls
        ChartFrame frame = new ChartFrame("Normative Simulation: Agents' Strength", xyc); 
        frame.pack(); 
        frame.setVisible(true); 
    }
    
    public void addSerie(String key, double[][] values) {
        dataset.addSeries(key, values);
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        g.setColor(Color.GRAY);
        g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        if (hmodel.hasObject(FoodModel.FOOD, x, y)) {
            drawFood(g, x, y);          
        }
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if (object == FoodModel.FOOD && !hmodel.hasObject(FoodModel.AGENT, x, y)) {
            drawFood(g, x, y);          
        }
    }

    public void drawFood(Graphics g, int x, int y) {
        g.setColor(Color.YELLOW);
        g.fillRect(x * cellSizeW + 15, y * cellSizeH+15, cellSizeW-30, cellSizeH-30);           
    }

}
