package jason.cli.agent;

import jason.cli.mas.RunningMASs;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.rmi.RemoteException;


@Command(
    name = "stop",
    description = "kills an agent"
)
public class StopAgent implements Runnable {
    
    @Parameters(paramLabel = "<agent name>", defaultValue = "",
               arity = "1",
               description = "agent unique identification")
    String agName;

    @CommandLine.Option(names = { "--mas-name" }, paramLabel = "<mas name>", defaultValue = "", description = "MAS unique identification")
    String masName;

    @CommandLine.ParentCommand
    protected Agent parent;

    @Override
    public void run() {
        if (!RunningMASs.isRunningMAS(masName)) {
            parent.parent.errorMsg("no running MAS, so, no agent to kill.");
            return;
        }
        if (agName.isEmpty()) {
            parent.parent.errorMsg("the name of the agent should be informed, e.g., 'agent stop bob'.");
            return;
        }
        if (!RunningMASs.hasAgent(masName, agName)) {
            parent.parent.errorMsg("the agent with name " + agName + " is not running!");
            return;
        }
        try {
            RunningMASs.getRTS(masName).killAgent(agName, null, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}

