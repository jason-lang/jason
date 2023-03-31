package jason.cli.app;

import jason.cli.JasonCommands;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "app",
    description = "commands to handle applications",
    subcommands = { Create.class, Compile.class, AddAgent.class, Gradle.class },
    synopsisSubcommandLabel = "(create | compile | add-agent | add-gradle )"
)
public class Application {

    @CommandLine.ParentCommand
    protected JasonCommands parent;

}

