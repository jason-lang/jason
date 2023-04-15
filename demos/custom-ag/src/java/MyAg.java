import jason.asSemantics.Agent;
import jason.asSyntax.ASSyntax;

@SuppressWarnings("serial")
public class MyAg extends Agent {

    public void initAg() {
        super.initAg();
        System.out.println("All arguments for this agent:\n\t"+getTS().getSettings().getUserParameters());

        // get myarg argument
        int n = Integer.valueOf( getTS().getSettings().getUserParameter("myarg") );
        System.out.println("myagr is "+n);

        // create n goals
        for (int i=0; i<n; i++) {
            try {
                getTS().getC().addAchvGoal( ASSyntax.parseLiteral("do("+i+")"), null);
            } catch (Exception e) {
                e.printStackTrace();
            }   
        }
    }
}
