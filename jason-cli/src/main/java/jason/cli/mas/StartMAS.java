package jason.cli.mas;

import jason.asSyntax.ASSyntax;
import jason.infra.local.RunLocalMAS;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

    @Option(names = { "--no-net" }, defaultValue = "false", description = "disable all net services (mind inspector, runtime services, Mbeans, ...")
    boolean noNet;

    @Option(names = { "--env" }, defaultValue = "", paramLabel = "<env class>", description = "class that implements the environment and its arguments")
    String envClass;
    @Option(names = { "--cp" }, defaultValue = "", paramLabel = "<classpath>", description = "directories where java classes can be found (for environment implementation, for instance)")
    String classPathArg;

    @CommandLine.ParentCommand
    protected MAS parent;

    @Override
    public void run() {
        if (RunningMASs.getLocalRunningMAS() != null) {
            parent.parent.errorMsg("this process can run only one MAS, that currently is "+RunningMASs.getLocalRunningMAS().getProject().getSocName());
            parent.parent.errorMsg("open another terminal for the new MAS, or stop the current one with 'mas stop'");
            return;
        }
        var args = new ArrayList<String>();

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

        args.add("--empty-mas");

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

        try {
            parent.parent.println("starting MAS "+masName+" ...");
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

            parent.parent.println("MAS "+masName+" is running.");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}

