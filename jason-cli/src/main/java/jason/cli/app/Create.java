package jason.cli.app;

import jason.asSyntax.ASSyntax;
import jason.util.Config;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


@Command(
    name = "create",
    description = "creates the files of a new application"
)
public class Create implements Runnable {

    @CommandLine.Parameters(paramLabel = "<MAS name>", defaultValue = "",
            arity = "1")
    String masName;

    @CommandLine.Option(names = { "--console" }, defaultValue = "false", description = "output will be sent to the console instead of a GUI")
    boolean console;

    @CommandLine.ParentCommand
    protected Application parent;

    @Override
    public void run() {
        if (masName.isEmpty()) {
            parent.parent.errorMsg("the name of the MAS should be informed, e.g., 'app create my_app1'.");
            return;
        }

        try {
            if (!ASSyntax.parseTerm(masName).isAtom()) {
                parent.parent.errorMsg("the name of the MAS should be a valid identifier (an atom).");
                return;
            }
        } catch (Exception e) {
            parent.parent.errorMsg("the name of the MAS should be a valid identifier (an atom).");
            return;
        }

        try {
            var path = new File(masName);
            if (path.exists()) {
                parent.parent.errorMsg("a directory for application '"+masName+"' exists already, chose another name for your MAS.");
                return;
            }

            createDirs(path);
            copyFiles(masName, path, console);
            usage(masName, path);
        } catch(Exception e) {
            parent.parent.errorMsg("error creating project: " + e);
            e.printStackTrace();
        }
    }

    void usage(String masName, File path) {
        parent.parent.println("\nYou can run your application with:");
        parent.parent.println("   $ jason "+path+"/"+masName+".mas2j");
    }

    void createDirs(File path) {
        if (!path.exists()) {
            System.out.println("Creating directory "+path);
            path.mkdirs();
        }
        new File(path + "/src/agt").mkdirs();
        new File(path + "/src/env/example").mkdirs();
    }

    void copyFiles(String masName, File path, boolean console) {
        copyFile(masName, "agent",              "bob",   new File( path + "/src/agt/bob.asl"), console);
        copyFile(masName, "agent",              "alice", new File( path + "/src/agt/alice.asl"), console);
        copyFile(masName, "project2.mas2j",     "",      new File( path + "/"+masName+".mas2j"), console);
        copyFile(masName, "environment2",       "",      new File( path + "/src/env/example/Env.java"), console);

        copyFile(masName, "logging.properties", "",      new File( path + "/logging.properties"), console);
    }

    public static void copyFile(String id, String source, String agName, File target, boolean consoleApp) {
        try (var in  = new BufferedReader(new InputStreamReader( Config.get().getDefaultResource(source) ));
             var out = new BufferedWriter(new FileWriter(target))) {
            String l = in.readLine();
            while (l != null) {
                l = l.replace("<PROJECT_NAME>", id);
                l = l.replace("<PROJECT-FILE>", id+".mas2j");
                l = l.replace("<PROJECT-FILE>", id+".mas2j");
                l = l.replace("<PROJECT-RUNNER-CLASS>", jason.infra.local.RunLocalMAS.class.getName());
                l = l.replace("<AG_NAME>", agName);
                l = l.replace("<VERSION>", Config.get().getJasonVersion());
                l = l.replace("<DATE>", new SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss").format(new Date()));

                if (consoleApp) {
                    l = l.replace("handlers = jason.runtime.MASConsoleLogHandler", "#handlers = jason.runtime.MASConsoleLogHandler");
                    l = l.replace("#handlers= java.util.logging.ConsoleHandler", "handlers= java.util.logging.ConsoleHandler");
                }
                out.append(l+"\n");
                l = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

