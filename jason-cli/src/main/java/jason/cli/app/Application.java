package jason.cli.app;

import jason.cli.JasonCommands;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "app",
    description = "commands to handle (the sources of) applications",
    subcommands = { Create.class, Compile.class, AddAgent.class, Gradle.class, AddInternalAction.class },
    synopsisSubcommandLabel = "(create | compile | add-agent | add-gradle | add-ia )"
)
public class Application {

    @CommandLine.ParentCommand
    protected JasonCommands parent;

}

