package jason.cli.mas;

import jason.infra.local.RunLocalMAS;
import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(
    name = "list",
    description = "lists current running MAS"
)
public class ListMAS implements Runnable {

    @CommandLine.ParentCommand
    protected MAS parent;

    @CommandLine.Option(names = { "--clean" }, defaultValue = "false", description = "remove all refs to existing running MAS")
    boolean clean;

    @Override
    public void run() {
        if (clean) {
            var f = RunLocalMAS.getRunningMASFile();
            parent.parent.println("deleting "+f);
            f.delete();
            return;
        }
        var all = RunningMASs.getAllRunningMAS();
        //parent.parent.println("running MAS:");
        for  (var mas: all.keySet()) {
            parent.parent.println("    "+mas+"@"+all.get(mas));
        }
    }
}

