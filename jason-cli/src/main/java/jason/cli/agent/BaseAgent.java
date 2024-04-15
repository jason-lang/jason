package jason.cli.agent;

import jason.cli.JasonCommands;
import jason.cli.mas.RunningMASs;


public class BaseAgent {
    
    public String testMasName(String masName, JasonCommands parent) {
        if (masName.isEmpty()) {
            masName = RunningMASs.getDefaultMASName();
            if (RunningMASs.isMASRunning(masName)) {
                if (!masName.isEmpty())
                    parent.println("using " + masName + " as MAS name");
            } else { // revert, masName is not working for some reason
                masName = "";
            }
        }
        return masName;
    }

    public void testRunningMAS(String masName, JasonCommands parent) {
        if (!RunningMASs.isMASRunning(masName)) {
            parent.errorMsg("no running MAS, create one with 'mas start'.");
        }
    }
}

