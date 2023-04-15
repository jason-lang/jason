package jason.cli.mas;

import jason.cli.JasonCommands;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "mas",
    description = "commands to handle running Multi-Agent Systems",
    subcommands = { StartMAS.class, StopMAS.class, ListMAS.class },
    synopsisSubcommandLabel = "(start | stop | list)"
)
public class MAS {

    @CommandLine.ParentCommand
    protected JasonCommands parent;

}

