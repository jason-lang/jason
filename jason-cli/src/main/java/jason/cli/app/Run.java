package jason.cli.app;

import jason.util.Config;

import java.io.File;


/** runs a mas2j using gradle */
public class Run extends Common {

    public void run(String mas2j, boolean verbose) {
        // use gradle to run
        var mas2jFile = new File(mas2j);
        if (!mas2jFile.exists()) {
            System.err.println("the application file "+mas2j+" does not exist!");
            return;
        }
        var projectDir = mas2jFile.getAbsoluteFile().getParentFile();

        var created = getOrCreateGradleFile( mas2j );

        if (verbose) {
            System.out.println(Config.get().getPresentation());
        }
        try (var connection = getGradleConnection(projectDir)) {
            getGradleBuild(connection, verbose, true)
                    .forTasks("run")
                    .run();

            if (created) {
                // delete created files
                new File(projectDir+"/build.gradle").delete();
                new File(projectDir+"/settings.gradle").delete();
            }
        } catch (Exception e) {
            System.err.println("Error running 'gradle run'");
        }
    }
}

