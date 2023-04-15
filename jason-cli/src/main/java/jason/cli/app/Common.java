package jason.cli.app;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.io.IOException;


public class Common {
    protected File getProjectFile(String masName) {
        if (!masName.isEmpty()) {
            if (!masName.endsWith(".mas2j"))
                masName += ".mas2j";

            var f = new File(masName);
            if (f.exists())
                return f;
        }

        // find a .mas2j file in current directory
        for (var nf: new File(".").listFiles()) {
            if (nf.getName().endsWith(".mas2j"))
                return nf;
        }

        return null;
    }
//    protected File createTempProjectFile(String masName) {
//        var f = new File( ".temp.mas2j");
//        CreateNewProject.copyFile("temp", "project", f, true);
//        return f;
//    }

    ProjectConnection getGradleConnection(File path) {
        return GradleConnector
                .newConnector()
                .forProjectDirectory(path)
                .connect();
    }

    BuildLauncher getGradleBuild(ProjectConnection conn) {
        return getGradleBuild(conn, true, true);
    }
    BuildLauncher getGradleBuild(ProjectConnection conn, boolean setStdOut, boolean setStdErr) {
        var b = conn.newBuild();
        if (setStdOut) b.setStandardOutput(System.out);
        if (setStdErr) b.setStandardError(System.err);
        return b;
    }

    boolean getOrCreateGradleFile(String masName) {
        var masFile = getProjectFile(masName);
        if (masFile == null)
            masFile = new File(".");
        var projectDir = masFile.getAbsoluteFile().getParentFile();
        try {
            projectDir = projectDir.getCanonicalFile();
        } catch (IOException e) {  }

        // checks settings.gradle
        var sets = new File(projectDir+"/settings.gradle");
        if (!sets.exists()) {
            try {
                sets.createNewFile();
            } catch (IOException e) {}
        }

        var f = new File(projectDir+"/build.gradle");
        if (f.exists())
            return false;

        if (masName.isEmpty()) {
            // masName based on directory name
            masName = projectDir.getName();
        }
        var p = masName.lastIndexOf(File.separatorChar);
        if (p>=0) {
            masName = masName.substring(p+1);
        }
        if (masName.endsWith(".mas2j")) {
            masName = masName.substring(0,masName.length()-6);
        }

        // create a temp file
        //f = new File(projectDir+"/.build-temp.gradle"); // does not work with gradle
        Create.copyFile(masName, "build.gradle", "", f, true);
        return true;
    }
}   

