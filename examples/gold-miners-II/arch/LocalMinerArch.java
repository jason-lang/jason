package arch;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;

/** architecture for local simulator */
public class LocalMinerArch extends MinerArch {


    /** this version of perceive is used in local simulator. it gets
        the perception and updates the world model. only relevant percepts
        are leaved in the list of perception for the agent.
      */
    @Override
    public Collection<Literal> perceive() {
        Collection<Literal> per = super.perceive();
        try {
            if (per != null) {
                Iterator<Literal> ip = per.iterator();
                while (ip.hasNext()) {
                    Literal p = ip.next();
                    String  ps = p.toString();
                    if (ps.startsWith("cell") && ps.endsWith("obstacle)") && model != null) {
                        int x = (int)((NumberTerm)p.getTerm(0)).solve();
                        int y = (int)((NumberTerm)p.getTerm(1)).solve();
                        if (x < model.getWidth() && y < model.getHeight())
                            obstaclePerceived(x, y, p);
                        ip.remove(); // the agent does not perceive obstacles

                    } else if (ps.startsWith("pos") && model != null) {
                        // announce my pos to others
                        int x = (int)((NumberTerm)p.getTerm(0)).solve();
                        int y = (int)((NumberTerm)p.getTerm(1)).solve();
                        if (x < model.getWidth() && y < model.getHeight())
                            locationPerceived(x, y);

                    } else if (ps.startsWith("carrying_gold") && model != null) {
                        // creates the model
                        int n = (int)((NumberTerm)p.getTerm(0)).solve();
                        carriedGoldsPerceived(n);

                        //} else if (ps.startsWith("cell") && ps.endsWith("ally)")  && model != null) {
                        //int x = (int)((NumberTerm)p.getTerm(0)).solve();
                        //int y = (int)((NumberTerm)p.getTerm(1)).solve();
                        //allyPerceived(x, y);
                        //ip.remove(); // the agent does not perceive Others

                    } else if (ps.startsWith("cell") && ps.endsWith("gold)")  && model != null) {
                        int x = (int)((NumberTerm)p.getTerm(0)).solve();
                        int y = (int)((NumberTerm)p.getTerm(1)).solve();
                        if (x < model.getWidth() && y < model.getHeight())
                            goldPerceived(x, y);

                    } else if (ps.startsWith("cell") && ps.endsWith("enemy)") && model != null) {
                        int x = (int)((NumberTerm)p.getTerm(0)).solve();
                        int y = (int)((NumberTerm)p.getTerm(1)).solve();
                        if (x < model.getWidth() && y < model.getHeight())
                            enemyPerceived(x, y);
                        //ip.remove(); // the agent does not perceive others

                    } else if (model == null && ps.startsWith("gsize")) {
                        // creates the model
                        int w = (int)((NumberTerm)p.getTerm(1)).solve();
                        int h = (int)((NumberTerm)p.getTerm(2)).solve();
                        setSimId(p.getTerm(0).toString());
                        gsizePerceived(w,h);
                        ip.remove();

                    } else if (model != null && ps.startsWith("steps")) {
                        // creates the model
                        int s = (int)((NumberTerm)p.getTerm(1)).solve();
                        stepsPerceived(s);
                        ip.remove();

                    } else if (ps.startsWith("depot")) {
                        int x = (int)((NumberTerm)p.getTerm(1)).solve();
                        int y = (int)((NumberTerm)p.getTerm(2)).solve();
                        if (x < model.getWidth() && y < model.getHeight())
                            depotPerceived(x, y);
                        ip.remove();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in perceive!", e);
        }
        return per;
    }
}
