package ia;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.centralised.RunCentralisedMAS;

@SuppressWarnings("serial")
public class create_ag_sst extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String agName = args[0].toString();
        String fName = ((StringTerm)args[1]).getString();
        
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fName))) {
            
            Agent ag = (Agent)in.readObject();

            // find cent ag arch
            AgArch arch =  ag.getTS().getUserAgArch().getFirstAgArch();
            while (arch != null && !(arch instanceof CentralisedAgArch)) {
                arch = arch.getNextAgArch();
            }
            CentralisedAgArch carch = (CentralisedAgArch)arch;
            carch.setTS(ag.getTS());

            carch.setAgName(agName);
            carch.getTS().setLogger(carch);
            carch.setLogger();
            ag.setLogger(carch);
            
            RunCentralisedMAS.getRunner().addAg(carch);
            
            // TODO: create a thread for the agent, ideally it should use the platform way to run the agent (pool, jade, ....)
            Thread agThread = new Thread(carch);
            carch.setThread(agThread);
            agThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;        
    }
}
