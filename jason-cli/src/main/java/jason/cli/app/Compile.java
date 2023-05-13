package jason.cli.app;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;


@Command(
    name = "compile",
    description = "compiles the java classes of the application in the current directory (using Gradle)"
)
public class Compile extends Common implements Runnable {

    @CommandLine.ParentCommand
    protected Application parent;

    @Override
    public void run() {
        var created = getOrCreateGradleFile("");

        try (var connection = getGradleConnection(new File("." ))) { //buildFile.getAbsoluteFile().getParentFile())) {
//            connection.model(GradleProject.class) // *** does not work
//                    .setStandardOutput(System.out)
//                    .withArguments("--build-file", buildFile.getAbsolutePath());
            getGradleBuild(connection)
                    .forTasks("compileJava")
                    .run();

            if (created) {
                // delete created files
                new File("build.gradle").delete();
                new File("settings.gradle").delete();
            }
        } catch(Exception e) {
            parent.parent.errorMsg("error compiling");
        }
    }
}

