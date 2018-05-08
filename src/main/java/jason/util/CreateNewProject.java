package jason.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class used to create an initial jason project:
 *
 * @author jomi
 */
public class CreateNewProject {

    File main, path;
    String id;
    static Config c = Config.get();

    public CreateNewProject(File m) {
        main = m;
        path = main.getAbsoluteFile(); //.getParentFile();

        id = main.getName();
        id = id.substring(0,1).toLowerCase() + id.substring(1);
        id = id.replace("-","_");
    }


    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(Config.get().getPresentation()+"\n");
            System.out.println("usage must be:");
            System.out.println("      java "+CreateNewProject.class.getName()+" <id of new application>");
            return;
        }

        CreateNewProject p = new CreateNewProject(new File(args[0]));
        p.createDirs();
        p.copyFiles();
        p.usage();
    }

    void usage() {
        System.out.println("\n\nYou can run your application with:");
        System.out.println("   $ jason "+path+"/"+id+".mas2j");
    }

    void createDirs() {
        if (!path.exists()) {
            System.out.println("Creating path "+path);
            path.mkdirs();
        }

        new File(path + "/lib").mkdirs();
    }

    void copyFiles() {
        copyFile("project", new File( path+"/"+id+".mas2j"));
        copyFile("agent", new File( path + "/sample_agent.asl"));
        copyFile("logging.properties", new File( path + "/logging.properties"));
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

                l = l.replace("agents:", "agents: sample_agent;");
                l = l.replace("<AG_NAME>", "sample_agent");

                l = l.replace("handlers = jason.runtime.MASConsoleLogHandler", "#handlers = jason.runtime.MASConsoleLogHandler");
                l = l.replace("#handlers= java.util.logging.ConsoleHandler", "handlers= java.util.logging.ConsoleHandler");

                out.append(l+"\n");
                l = in.readLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
