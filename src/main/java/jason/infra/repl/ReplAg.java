package jason.infra.repl;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Trigger;

public abstract class ReplAg extends Agent {
    
    String[] replCmds = { 
            clear.class.getName(),
            //verbose.class.getName(),
            mi.class.getName()};
    
    int cmdCounter = 0;
    
    @Override
    public void initAg() {
        super.initAg();
    }
    
    @Override
    public void load(String asSrc) throws JasonException {
        super.load(null);
        /*try {
            getPL().add(ASSyntax.parsePlan("+!run_repl_expr(Cmd__TR) <- Cmd__TR; jason.infra.repl.print_unifier."));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }
    
    void execCmd(String sCmd) {
        try {
            if (sCmd.endsWith(".")) 
                sCmd = sCmd.substring(0,sCmd.length()-1);
            for (String c: replCmds) {
                if (c.endsWith(sCmd) && sCmd.startsWith(".")) {
                    sCmd = c;
                    break;
                }
            }
            if (sCmd.startsWith(".verbose")) {
                sCmd = verbose.class.getPackage().getName() + sCmd;
            }
            sCmd += ";"+print_unifier.class.getName();
            PlanBody lCmd = ASSyntax.parsePlanBody(sCmd);
            Trigger  te   = ASSyntax.parseTrigger("+!run_repl_expr");
            Intention i   = new Intention();
            i.push(new IntendedMeans(
                    new Option(
                            new Plan(null,te,null,lCmd),
                            new Unifier()), 
                    te));
            //Literal g = ASSyntax.createLiteral("run_repl_expr", lCmd);
            //getTS().getLogger().info("running "+i);
            //getTS().getC().addAchvGoal(g, null);
            getTS().getC().addIntention(i);
            cmdCounter++;
            clear();
            getTS().getUserAgArch().wake();
        } catch (Exception e) {
            print("Error parsing "+sCmd+"\n"+e);
        }        
    }

    public void print(String s) {
        System.out.println(s+"\n");
    }
    
    public void clear() {
    }
    
}
