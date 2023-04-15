package jason.cli.mas;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;


@Command(
    name = "stop",
    description = "stops a MAS"
)
public class StopMAS implements Runnable {
    

    @CommandLine.Parameters(paramLabel = "<mas name>", defaultValue = "",
            arity = "0..1",
            description = "MAS unique identification")
    String masName;


    @Option(names = { "--exit" }, description = "stops the MAS and terminates the process")
    boolean exit;

    @Option(names = "--deadline",
            paramLabel = "<deadline>", defaultValue = "0",
            description = "the amount of time (in milliseconds) to wait for stopping the MAS")
    int deadline;

    @ParentCommand
    private MAS parent;

    @Override
    public void run() {
        if (masName.isEmpty() && RunningMASs.isRunningMAS(null) ||
                RunningMASs.isRunningMAS(null) && RunningMASs.getLocalRunningMAS().getProject().getSocName().equals(masName)) {
            // stop the local running MAS
            var localMAS = RunningMASs.getLocalRunningMAS();
            if (exit || !parent.parent.isTerminal()) {
                //parent.parent.println(localMAS.getName()+" stopped");
                localMAS.finish(deadline, true, 0);
                System.exit(0);
            } else {
                localMAS.finish(deadline, false, 0);
                //parent.parent.println(localMAS.getName()+" stopped");
            }
            return;
        } else {
            var rt = RunningMASs.getRTS(masName);
            if (rt != null) {
                try {
                    rt.stopMAS(deadline, exit, 0);
                    return;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        parent.parent.errorMsg("could not find an MAS to stop, run 'mas list' to see the list of running MAS.");
    }

}

