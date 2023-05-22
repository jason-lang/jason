package jason.cli;

import jason.cli.agent.Agent;
import jason.cli.app.Application;
import jason.cli.mas.MAS;
import jason.util.Config;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

import java.io.PrintWriter;

// program "inspired" by https://github.com/remkop/picocli/tree/v4.7.1/picocli-shell-jline3

@Command(name = "jason",
        // version = "1.0",
        versionProvider = jason.cli.VersionProvider.class,
        mixinStandardHelpOptions = true,
        subcommands = {  Application.class, MAS.class, Agent.class, Echo.class, Wait.class },
        synopsisSubcommandLabel = "(app | mas | agent | <mas2j  file>)"
)
public class JasonCommands {

    private PrintWriter out = null;
    private PrintWriter err = null;

    public PrintWriter getOut() {
        return out;
    }

    public void println(String s) {
        if (out == null) {
            System.out.println(s);
        } else {
            out.println(s);
            out.flush();
        }
    }
    public void errorMsg(String s) {
        if (out == null) {
            System.err.println(s);
        } else {
            out.println(s);
        }
    }

    public boolean isTerminal() {
        return out != null;
    }

    public void setReader(LineReader reader) {
        out = reader.getTerminal().writer();
    }
}
   
class VersionProvider implements IVersionProvider {
    public String[] getVersion() {
        return new String[] { "Jason CLI " + Config.get().getJasonVersion() };
    }
}

@Command(name = "echo",  hidden = true)
class Echo implements Runnable {

    @CommandLine.ParentCommand
    protected JasonCommands parent;

    @CommandLine.Parameters(paramLabel = "<message>", defaultValue = "")
    String msg;

    @Override
    public void run() {
        parent.println(msg);
    }
}
@Command(name = "wait",  hidden = true)
class Wait implements Runnable {

    @CommandLine.ParentCommand
    protected JasonCommands parent;

    @CommandLine.Parameters(paramLabel = "<time in ms>", defaultValue = "1000")
    int time;

    @Override
    public void run() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}
