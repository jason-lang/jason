package jason.cli.agent;

import jason.cli.JasonCommands;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "agent",
    description = "commands to handle (running) agents",
    subcommands = { StartAgent.class, StopAgent.class, ListAgents.class, RunAsAgent.class, MindAgent.class, StatusAgent.class, LoadIntoAgent.class },
    synopsisSubcommandLabel = "(start | stop | list | run-as | load-into | mind | status)"
)
public class Agent {

    @CommandLine.ParentCommand
    protected JasonCommands parent;

}

