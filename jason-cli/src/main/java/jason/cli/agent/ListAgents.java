package jason.cli.agent;

import jason.cli.mas.RunningMASs;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.rmi.RemoteException;


@Command(
    name = "list",
    description = "lists running agents"
)
public class ListAgents extends BaseAgent implements Runnable {

    @CommandLine.ParentCommand
    protected Agent parent;

    @CommandLine.Option(names = { "--mas-name" }, paramLabel = "<mas name>", defaultValue = "", description = "MAS unique identification")
    String masName;

    @Override
    public void run() {
        masName = testMasName(masName, parent.parent);
        testRunningMAS(masName, parent.parent);

        try {
            var rts = RunningMASs.getRTS(masName);
            if (rts != null) {
                for (var ag : rts.getAgentsName()) {
                    parent.parent.println("    " + ag);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

