package jason.cli.app;

import jason.asSemantics.DefaultInternalAction;
import jason.util.Config;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;


@Command(
    name = "add-ia",
    description = "adds the source code a new internal action into the application"
)
public class AddInternalAction extends Common implements Runnable {

    @CommandLine.Parameters(paramLabel = "<name>", defaultValue = "", description = "inform the full name of the class (package.className).",
            arity = "1")
    String iaFullName;


    @CommandLine.ParentCommand
    protected Application parent;

    @Override
    public void run() {
        if (iaFullName.isEmpty()) {
            parent.parent.errorMsg("the name of the internal action should be informed, e.g., 'app add-ia my_pkg.my_print'.");
            return;
        }

        try {
            var file = getProjectFile("");
            if (file == null) {
                parent.parent.errorMsg("can not find a .mas2j file in the current directory!");
                return;
            }

            var lastDot = iaFullName.lastIndexOf(".");
            var iaName = iaFullName.substring(lastDot+1);
            var pkgName = iaFullName.substring(0,lastDot);

            var iaFile = new File("src/java/"+pkgName.replace(".","/")+"/"+iaName+".java");
            if (iaFile.exists()) {
                parent.parent.errorMsg("a file '"+iaFile+"' exists already, chose another name for the internal action.");
                return;
            }

            copyFileIA("", "ia", pkgName, iaName, iaFile);
            parent.parent.println("internal action "+iaFullName+" ("+iaFile+") created.");
        } catch(Exception e) {
            parent.parent.errorMsg("error adding agent:\n" + e);
        }
    }

    public void copyFileIA(String id, String source, String iaPkg, String iaName, File target) {
        var dir = new File("src/java/"+iaPkg.replace(".","/"));
        if (!dir.exists())
            dir.mkdirs();
        try (var in  = new BufferedReader(new InputStreamReader( Config.get().getDefaultResource(source) ));
             var out = new BufferedWriter(new FileWriter(target))) {
            String l = in.readLine();
            while (l != null) {
                l = l.replace("<PROJECT_NAME>", id);
                l = l.replace("<PCK>", iaPkg);
                l = l.replace("<PCK>", iaPkg);
                l = l.replace("<IA_NAME>", iaName);
                l = l.replace("<IA_NAME>", iaName);
                l = l.replace("<SUPER_CLASS>", DefaultInternalAction.class.getName());
                out.append(l+"\n");
                l = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

