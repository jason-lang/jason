package my;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

/** Plot a graph with the score of the players */
public class plot extends DefaultInternalAction {
    
    Map<Integer,Integer> values = new HashMap<Integer,Integer>();

    static DefaultXYDataset dataset = new DefaultXYDataset();
    static {
        JFreeChart xyc = ChartFactory.createXYLineChart( 
                "Players' score",
                "step",
                "score",
                dataset, // dataset, 
                PlotOrientation.VERTICAL, // orientation, 
                true, // legend, 
                true, // tooltips, 
                true); // urls
        
        JFrame frame = new ChartFrame("Iterated Prisioners Dilemma", xyc);
        frame.setSize(800,500);
        frame.setVisible(true);
    }
    
    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        int step  = (int)((NumberTerm)args[0]).solve();
        int score = (int)((NumberTerm)args[1]).solve();
        addValue(ts.getUserAgArch().getAgName(), step, score);
        return true;
    }

    void addValue(String agName, int step, int vl) {
        values.put(step,vl);
        double[][] data = getData(step);
        synchronized (dataset) {
            dataset.addSeries(agName, data);            
        }
    }
    
    private double[][] getData(int maxStep) {
        double[][] r = new double[2][maxStep+1];
        int vl = 0;
        for (int step = 0; step<=maxStep; step++) {
            if (values.containsKey(step))
                vl = values.get(step);            
            r[0][step] = step;
            r[1][step] = vl;
        }
        return r;
    }
}
