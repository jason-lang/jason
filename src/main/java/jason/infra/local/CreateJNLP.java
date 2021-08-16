package jason.infra.local;

import java.io.File;
import java.io.PrintWriter;

import jason.util.Config;

/** creates a JNLP file for a project */
public class CreateJNLP {
    public static void main(String[] args) {
        try {
            String projectName = args[0];
            String mas2jFile   = args[1];

            String file = projectName+".jnlp";
            PrintWriter out = new PrintWriter(new File(file));

            String script = Config.get().getTemplate("jnlp-template.xml");
            script = LocalMASLauncherAnt.replace(script, "<PROJECT-ID>", projectName);
            script = LocalMASLauncherAnt.replace(script, "<PROJECT-ID>", projectName);
            script = LocalMASLauncherAnt.replace(script, "<PROJECT-ID>", projectName);
            script = LocalMASLauncherAnt.replace(script, "<PROJECT-ID>", projectName);
            script = LocalMASLauncherAnt.replace(script, "<PROJECT-ID>", projectName);

            String defCodebase = "http://localhost";
            try {
                defCodebase = "file:" + new File(mas2jFile).getAbsoluteFile().getParentFile().getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }

            script = LocalMASLauncherAnt.replace(script, "<DEFAULT-CODEBASE>", defCodebase);

            String jars = "";
            File lib = new File("../lib");
            if (lib.exists()) {
                for (String j: new File("../lib").list()) {
                    System.out.println(j);
                    if (j.endsWith("jar")) {
                        jars += "  <jar href=\""+j+"\" />\n";
                    }
                }
            }
            script = LocalMASLauncherAnt.replace(script, "<OTHER-JARS>", jars);
            out.write(script);

            out.close();
            System.out.print("File "+file+" created!");

        } catch (Exception e) {
            System.err.println("Error creating the jnlp file:");
            e.printStackTrace();
        }
    }
}
