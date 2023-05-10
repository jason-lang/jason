package jason.cli.agent;

import jason.cli.mas.RunningMASs;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.util.List;


@Command(
    name = "load-into",
    description = "loads some ASL code into a running agent"
)
public class LoadIntoAgent implements Runnable {
    
    @Parameters(paramLabel = "<agent name>", defaultValue = "",
               arity = "1",
               description = "agent unique identification")
    String agName;

    @CommandLine.Option(names = { "--source" }, defaultValue = "", paramLabel = "<source file>", description = "file (or URL) for the source code of the agent.")
    String sourceFile;

    @CommandLine.Option(names = { "--source-id" }, defaultValue = "", paramLabel = "<string>", description = "identifiers of the plans")
    String sourceId;

    @CommandLine.Option(names = { "--mas-name" }, paramLabel = "<mas name>", defaultValue = "", description = "MAS identification where to find the agent")
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
            parent.parent.errorMsg("the name of the new agent should be informed, e.g., 'agent load-into bob'.");
            return;
        }
        if (!RunningMASs.hasAgent(masName, agName)) {
            parent.parent.errorMsg("the agent with name " + agName + " is not running!");
            return;
        }

        boolean replace = false;
        if (sourceId.isEmpty()) {
            sourceId = "jason-cli";
        } else {
            replace = true;
        }

        var code = getCode(sourceFile, allParameters);
        try {
            if (!code.isEmpty()) {
                RunningMASs.getRTS(masName).loadASL(agName, code, sourceId, replace);
            } else {
                parent.parent.errorMsg("no code informed. E.g.: agent load-into bob { +!g <- .print(ok). }");
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCode(String sourceFile, List<String> allParameters) {
        var code = new StringBuilder();
        if (!sourceFile.isEmpty()) {
            // load the source file and sent the string to the server (since the files are not there)
            try (var in = new BufferedReader(new FileReader(sourceFile))) {
                var line = in.readLine();
                while (line != null) {
                    code.append(line).append("\n");
                    line = in.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // load code in the script, enclosed by { }
        if (allParameters.size()>0) {
            var last = allParameters.get( allParameters.size()-1).trim();
            if (last.startsWith("{")) {
                code.append(last.substring(1, last.length() - 1).trim());
            }
        }

        return code.toString();
    }
}

