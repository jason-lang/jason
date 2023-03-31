package jason.cli.mas;

import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(
    name = "list",
    description = "lists current running MAS"
)
public class ListMAS implements Runnable {

    @CommandLine.ParentCommand
    protected MAS parent;

    @Override
    public void run() {
        var all = RunningMASs.getAllRunningMAS();
        //parent.parent.println("running MAS:");
        for  (var mas: all.keySet()) {
            parent.parent.println("    "+mas+" @"+all.get(mas));
        }
    }
}

