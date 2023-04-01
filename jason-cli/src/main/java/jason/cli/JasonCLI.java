package jason.cli;

import jason.cli.app.Run;
import jason.cli.mas.RunningMASs;
import jason.util.Config;
import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.ConfigurationPath;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.DefaultParser.Bracket;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import org.jline.widget.Widgets;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

// program "inspired" by https://github.com/remkop/picocli/tree/v4.7.1/picocli-shell-jline3

public class JasonCLI {


    public static void main(String[] args) {
        // add jason package
        try {
            var jasonBin = JasonCLI.class.getProtectionDomain().getCodeSource().getLocation().toString();
            if (jasonBin.startsWith("file:"))
                jasonBin = jasonBin.substring(5);
            Config.get().addPackage("jason", new File(jasonBin));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (args.length == 0) {
            startTerminal();
        } else if (args.length >= 1 && args[0].endsWith(".mas2j")) {
            // case of .mas2j
            new Run().run(args[0], args.length == 2 && args[1].equals("-v"));
        } else {
            int exitCode = new CommandLine(new JasonCommands()).execute(args);
            if (!RunningMASs.hasLocalRunningMAS())
                System.exit(exitCode);
        }
    }

    static void startTerminal() {
        AnsiConsole.systemInstall();
        try {
            Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
            var confPath = new ConfigurationPath(
                    Paths.get(System.getProperty("user.dir")),
                    Paths.get(System.getProperty("user.home"))
            );

            // set up jason commands
            var jasonCommands = new JasonCommands();

            PicocliCommandsFactory factory = new PicocliCommandsFactory();
            // Or, if you have your own factory, you can chain them like this:
            // MyCustomFactory customFactory = createCustomFactory(); // your application custom factory
            // PicocliCommandsFactory factory = new PicocliCommandsFactory(customFactory); // chain the factories

            CommandLine cmd = new CommandLine(jasonCommands, factory);
            PicocliCommands picocliCommands = new PicocliCommands(cmd);

            var parser = new DefaultParser();
            parser.setEofOnUnclosedBracket(Bracket.CURLY); //, Bracket.ROUND, Bracket.SQUARE);
            parser.setEofOnUnclosedQuote(true);
            parser.blockCommentDelims(new DefaultParser.BlockCommentDelims("/*", "*/"))
                    .lineCommentDelims(new String[] {"#", "//"});

            try (var terminal = TerminalBuilder.builder().build()) {
                SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, confPath);
                systemRegistry.setCommandRegistries(picocliCommands);
                systemRegistry.register("help", picocliCommands);

                var reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(systemRegistry.completer())
                        .parser(parser)
                        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true) // to not have problems is ! used by jason
                        .build();

                jasonCommands.setReader(reader);
                factory.setTerminal(terminal);

                TailTipWidgets widgets = new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER);
                widgets.enable();
                var keyMap = reader.getKeyMaps().get("main");
                keyMap.bind(new Reference(Widgets.TAILTIP_TOGGLE), KeyMap.alt("s"));

                String prompt = "jason> ";
                //System.out.println(terminal.getName() + ": " + terminal.getType());
                if (terminal.getType().startsWith("dumb")) { // by this terminal type, I assume it is reading a script from a file
                    prompt = "";
                } else {
                    terminal.writer().println("Jason interactive shell with completion and autosuggestions.");
                    terminal.writer().println("      Hit <TAB> to see available commands.");
                    terminal.writer().println("      Press Ctrl-D to exit."); //(terminal type="+terminal.getType()+ ")");

                    reader.variable(LineReader.INDENTATION, 4)
                            .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                            .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home")+"/.jason-cli", "history"));
                }

                // start the shell and process input until the user quits with Ctrl-D
                String line;
                while (true) {
                    try {
                        systemRegistry.cleanUp();
                        line = reader.readLine(prompt).trim();
                        systemRegistry.execute(line);
                    } catch (UserInterruptException e) {
                        systemRegistry.trace(e);
                    } catch (EndOfFileException e) {
                        if (terminal.getType().equals("dumb-color")) {
                            reader.getTerminal().writer().println("\n<end of script>");
                        }
                        if (RunningMASs.hasLocalRunningMAS()) {
                            terminal.writer().println("an MAS is running... stop it to end this process or <CTRL>-C.");
                        }
                        return;
                    } catch (Exception e) {
                        systemRegistry.trace(e);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }
}
