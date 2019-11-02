package jason.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * class used to create an initial jason project:
 *
 * @author jomi
 */
public class CreateNewProject {

    File main, path;
    String id;
    static Config c = Config.get();
    boolean consoleApp = false;

    public CreateNewProject(File m) {
        main = m;
        path = main.getAbsoluteFile(); //.getParentFile();

        id = main.getName();
        id = id.substring(0,1).toLowerCase() + id.substring(1);
        id = id.replace("-","_");
    }


    public static void main(String[] args) throws Exception {
        boolean consoleApp = false;
        for (int i=0; i<args.length; i++)
            if ("--console".equals(args[i]))
                consoleApp = true;

        String pId = null;
        if (args.length == 0 || (args.length == 1 && consoleApp)) {
            System.out.println(Config.get().getPresentation()+"\n");
            System.out.print("\n\nEnter the identification of the new application: ");
            pId = new Scanner(System.in).nextLine();
            if (pId.length() == 0) {
                System.out.println("      you should enter a project id!");
                return;
            }

            //System.out.println("usage must be:");
            //System.out.println("      java "+CreateNewProject.class.getName()+" <id of new application>");
            //return;
        } else {
            pId = args[0];
        }

        if (Config.get().getJasonHome().isEmpty()) {
            Config.get().setShowFixMsgs(false);
            Config.get().fix();
        }

        CreateNewProject p = new CreateNewProject(new File(pId));
        p.consoleApp = consoleApp;
        p.createDirs();
        p.copyFiles();
        p.usage();
    }

    void usage() {
        System.out.println("\n\nYou can run your application with:");
        System.out.println("   $ jason "+path+"/"+id+".mas2j");
        System.out.println("or");
        System.out.println("   $ cd "+path);
        System.out.println("   $ gradle -q --console=plain\n");
        System.out.println("an eclipse project can be created with");
        System.out.println("   $ gradle eclipse");
        System.out.println("or 'Gradle Import Project' from Eclipse menu File/Import\n");
    }

    void createDirs() {
        if (!path.exists()) {
            System.out.println("Creating path "+path);
            path.mkdirs();
        }

        //new File(path + "/lib").mkdirs();
    }

    void copyFiles() {
        copyFile("project", new File( path+"/"+id+".mas2j"));
        copyFile("agent", new File( path + "/sample_agent.asl"));
        copyFile("logging.properties", new File( path + "/logging.properties"));
        copyFile("build.gradle", new File( path + "/build.gradle"));
    }

    void copyFile(String source, File target) {
        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( c.getDetaultResource(source) ));
            BufferedWriter out = new BufferedWriter(new FileWriter(target));
            String l = in.readLine();
            while (l != null) {
                l = l.replace("<PROJECT_NAME>", id);
                l = l.replace("<PLATFORM>", "");
                l = l.replace("<INFRA>", "Centralised");
                l = l.replace("<VERSION>", c.getJasonVersion());
                l = l.replace("<DATE>", new SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss").format(new Date()));
                l = l.replace("<PROJECT-RUNNER-CLASS>", jason.infra.centralised.RunCentralisedMAS.class.getName());
                l = l.replace("<PROJECT-FILE>", id+".mas2j");

                l = l.replace("agents:", "agents: sample_agent;");
                l = l.replace("<AG_NAME>", "sample_agent");

                if (consoleApp) {
                    l = l.replace("handlers = jason.runtime.MASConsoleLogHandler", "#handlers = jason.runtime.MASConsoleLogHandler");
                    l = l.replace("#handlers= java.util.logging.ConsoleHandler", "handlers= java.util.logging.ConsoleHandler");
                }
                out.append(l+"\n");
                l = in.readLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
