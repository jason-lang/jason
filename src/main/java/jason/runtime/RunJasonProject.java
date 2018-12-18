package jason.runtime;

import java.io.File;

import jason.infra.MASLauncherInfraTier;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.mas2j;
import jason.util.Config;

/**
 * Run a Jason mas2j project
 *
 * parameters:
 *    /Jason mas2j Project File/
 *
 * @author jomi
 *
 */
public class RunJasonProject {

    static MASLauncherInfraTier launcher;

    // Run the parser
    public static void main (String args[]) {

        String name;
        mas2j parser;
        MAS2JProject project = new MAS2JProject();

        if (args.length == 0) {
            System.out.println(Config.get().getPresentation()+"\n");
            System.out.println("usage must be:");
            System.out.println("      java "+RunJasonProject.class.getName()+" <MAS2j Project File>");
            return;
        } else {
            name = args[0];
            System.err.println("reading from file " + name + " ..." );
            try {
                parser = new mas2j(new java.io.FileInputStream(name));
            } catch(java.io.FileNotFoundException e) {
                System.err.println("file \"" + name + "\" not found.");
                return;
            }
        }

        // parsing
        try {
            File file = new File(name);
            project = parser.mas();
            if (Config.get().getJasonJar() == null) {
                System.out.println("Jason is not configured, creating a default configuration");
                Config.get().fix();
            }
            project.setProjectFile(file);
            project.setDirectory(file.getAbsoluteFile().getParentFile().getAbsolutePath());
            System.out.println("file "+name+" parsed successfully!\n");

            launcher = project.getInfrastructureFactory().createMASLauncher();
            launcher.setProject(project);
            launcher.writeScripts(false, false);

            new Thread(launcher, "MAS-Launcher").start();
        } catch(Exception e) {
            System.err.println("parsing errors found... \n" + e);
        }
    }

    public MASLauncherInfraTier getLauncher() {
        return launcher;
    }

}
