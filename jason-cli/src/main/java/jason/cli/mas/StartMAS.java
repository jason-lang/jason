package jason.cli.mas;

import jason.asSyntax.ASSyntax;
import jason.cli.app.Run;
import jason.infra.local.RunLocalMAS;
import jason.mas2j.parser.mas2j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


@Command(
    name = "start",
    description = "starts a new (empty) MAS"
)
public class StartMAS implements Runnable {
    
    static private int masCount = 1;

    @Parameters(paramLabel = "<mas name>", defaultValue = "",
               arity = "0..1",
               description = "MAS unique identification")
    String masName;

    @Option(names = { "--console" }, defaultValue = "false", description = "output will be sent to the console instead of a GUI")
    boolean console;

    @Option(names = { "--no-net" }, defaultValue = "false", description = "disable all net services (mind inspector, runtime services, Mbeans, ...)")
    boolean noNet;

    @Option(names = { "--env" }, defaultValue = "", paramLabel = "<env class>", description = "class that implements the environment and its arguments")
    String envClass;
    @Option(names = { "--cp" }, defaultValue = "", paramLabel = "<classpath>", description = "directories where java classes can be found (e.g., for environment implementation)")
    String classPathArg;

    @Option(names = { "--mas2j" }, defaultValue = "", paramLabel = "<mas2j file>", description = "runs a Jason project")
    String mas2j;

    @Option(names = { "--use-gradle" }, defaultValue = "false", description = "executes the MAS defined in a mas2j file using gradle")
    boolean useGradle;

    @CommandLine.ParentCommand
    protected MAS parent;

    @Override
    public void run() {
        if (RunningMASs.getLocalRunningMAS() != null) {
            parent.parent.errorMsg("this process can run only one MAS, that currently is "+RunningMASs.getLocalRunningMAS().getProject().getSocName());
            parent.parent.errorMsg("open another terminal for the new MAS, or stop the current one with 'mas stop'");
            return;
        }

        // get MAS name from .mas2j
        if (masName.isEmpty() && !mas2j.isEmpty()) {
            try {
                var parser = new mas2j(new FileInputStream(mas2j));
                var project = parser.mas();
                masName = project.getSocName();
                parent.parent.println("MAS name (from mas2j) is " + masName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (useGradle) {
            if (mas2j.isEmpty()) {
                parent.parent.errorMsg("a mas2j file should be informed. E.g., jason mas start --use-gradle --mas2j=t.mas2j");
                return;
            }

            if (parent.parent.isTerminal())
                new Thread(() -> new Run().run(mas2j, true)).start();
            else
                new Run().run(mas2j, true);
            waitRunning(masName);
            return;
        }

        var existing = RunningMASs.getAllRunningMAS().keySet();
        if (masName.isEmpty()) {
            masName = "mas_" + (masCount++);
            while (existing.contains(masName)) {
                masName = "mas_" + (masCount++);
            }
        } else if (existing.contains(masName)) {
            parent.parent.errorMsg("there is an MAS named "+masName+" already running, select another name");
            return;
        }

        try {
            if (!ASSyntax.parseTerm(masName).isAtom()) {
                parent.parent.errorMsg("the name of the MAS should be a valid identifier, e.g., 'mas start m1'.");
                return;
            }
        } catch (Exception e) {
            parent.parent.errorMsg("the name of the MAS should be a valid identifier, e.g., 'mas start m1'.");
            return;
        }

        var args = new ArrayList<String>();

        if (mas2j.isEmpty()) {
            args.add("--empty-mas");
        } else {
            args.add(0, mas2j);
            masName = "";
        }

        if (console) {
            args.add("--log-conf");
            args.add("$jason/templates/console-logging.properties");
        }
        if (noNet) {
            args.add("--no-net");
        }
        var classPathList = new ArrayList<String>();
        for (var p: classPathArg.split(":"))
            if (!p.trim().isEmpty())
                classPathList.add(p.trim());
        classPathList.add(".");
        classPathList.add("build/classes/java/main");
        classPathList.add("bin/classes/");
        File libs = new File("lib");
        if (libs.exists()) {
            for (var f: libs.list()) {
                if (f.endsWith(".jar")) {
                    classPathList.add("lib/"+f);
                }
            }
        }
        //System.out.println("ClassPath="+classPathList);

        try {
            parent.parent.println("starting MAS "+masName+"...");
            if (args.size() > 1)
                parent.parent.println("         "+args);

            // the application has a specific classloader that consider the application classpath
            var cl = new MasAppClassLoader(
                    getClass().getClassLoader(),
                    classPathList
            );
            var mclass = cl.loadClass(CLILocalMAS.class.getName());
            var r = (RunLocalMAS)mclass.getDeclaredConstructor().newInstance();
            r.addInitArg("masName", masName);
            r.addInitArg("envName", envClass);
            r.init(args.toArray(new String[args.size()]));
            r.create();
            new Thread( (Runnable) r).start();

            waitRunning(masName);

            parent.parent.println("MAS "+masName+" is running.");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    void waitRunning(String masName) {
        var c = 1;
        while (c<30 && !RunningMASs.isMASRunning(masName)) {
            if (c>5)
                parent.parent.println("Waiting '"+masName+"' to start. ("+c+")");
            try {
                Thread.sleep(100+(c*100));
            } catch (InterruptedException e) {
                break;
            }
            c++;
        }
    }
}

