package jason.cli.app;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;


@Command(
    name = "add-gradle",
    description = "adds a Gradle script for the application in the current directory"
)
public class Gradle extends Common implements Runnable {

    @CommandLine.ParentCommand
    protected Application parent;

    @Override
    public void run() {
        var created = getOrCreateGradleFile( "" );
        try (var connection = getGradleConnection(new File("." ))) { //gradleFile.getAbsoluteFile().getParentFile())
            getGradleBuild(connection)
                    .forTasks("wrapper")
                    .run();
        } catch (Exception e) {
            System.err.println("Error creating gradle wrapper");
        }
        if (created)
            parent.parent.println("\n\nfile build.gradle created.");
        parent.parent.println("\nyou can execute your application with:");
        parent.parent.println("    ./gradlew run");
    }
}

