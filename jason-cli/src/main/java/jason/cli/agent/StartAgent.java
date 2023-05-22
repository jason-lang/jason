package jason.cli.agent;

import jason.asSyntax.ASSyntax;
import jason.cli.mas.RunningMASs;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


@Command(
    name = "start",
    description = "starts a new (empty) agent"
)
public class StartAgent implements Runnable {
    
    @Parameters(paramLabel = "<agent name>", defaultValue = "",
               arity = "1",
               description = "agent unique identification")
    String agName;

    @CommandLine.Option(names = { "--instances" }, defaultValue = "1", description = "how many agents should be created")
    int instances;

    @CommandLine.Option(names = { "--source" }, defaultValue = "", paramLabel = "<source file>", description = "file (or URL) for the source code of the agent.")
    String sourceFile;

    @CommandLine.Option(names = { "--source-id" }, defaultValue = "", paramLabel = "<string>", description = "identifiers of the plans")
    String sourceId;

    @CommandLine.Option(names = { "--mas-name" }, paramLabel = "<mas name>", defaultValue = "", description = "MAS unique identification")
    String masName;

    @Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
    List<String> allParameters; // no "index" attribute: captures _all_ arguments

    @CommandLine.ParentCommand
    protected Agent parent;

    @Override
    public void run() {
        if (!RunningMASs.isRunningMAS(masName)) {
            parent.parent.errorMsg("no running MAS, create one with 'mas start'.");
            return;
        }
        if (agName.isEmpty()) {
            parent.parent.errorMsg("the name of the new agent should be informed, e.g., 'agent start bob'.");
            return;
        }
        try {
            if (!ASSyntax.parseTerm(agName).isAtom()) {
                parent.parent.errorMsg("the name of the new agent should be a valid identifier, e.g., 'agent start bob'.");
                return;
            }
        } catch (Exception e) {
            parent.parent.errorMsg("the name of the new agent should be a valid identifier, e.g., 'agent start bob'.");
            return;
        }

        // create the agents
        var ags = new ArrayList<String>();
        var rt =  RunningMASs.getRTS(masName);
        try {
            for (int i=0; i<instances; i++) {
                var n = agName;
                if (instances>1)
                    n = agName + i;
                ags.add(rt.createAgent( n, null, null, null, null, null, null));
            }
        } catch (Exception e) {
            parent.parent.errorMsg("error creating agent: "+e.getMessage());
            return;
        }

        boolean replace = false;
        if (sourceId.isEmpty()) {
            sourceId = "jason-cli";
        } else {
            replace = true;
        }

        String code = LoadIntoAgent.getCode(sourceFile, allParameters);

        // load code into agents
        for (String a: ags) {
            // load code informed as parameter
            if (code != null && !code.isEmpty()) {
                try {
                    rt.loadASL(agName, code, sourceId, replace);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                rt.startAgent(a);
                parent.parent.println("agent " + a + " started.");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

