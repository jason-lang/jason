package jason.cli.app;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;


@Command(
    name = "add-agent",
    description = "adds a new agent into the application"
)
public class AddAgent extends Common implements Runnable {

    @CommandLine.Parameters(paramLabel = "<agent name>", defaultValue = "",
            arity = "1")
    String agName;


    @CommandLine.ParentCommand
    protected Application parent;

    @Override
    public void run() {
        if (agName.isEmpty()) {
            parent.parent.errorMsg("the name of the agent should be informed, e.g., 'app add-agent karlos'.");
            return;
        }

        try {
            var file = getProjectFile("");
            if (file == null) {
                parent.parent.errorMsg("can not find a .mas2j file in the current directory!");
                return;
            }

            var agFile = new File("src/agt/"+agName+".asl");
            if (agFile.exists()) {
                parent.parent.errorMsg("a file '"+agFile+"' exists already, chose another name for the agent.");
                return;
            }

            Create.copyFile("", "agent", agName, agFile, true);
            addAgInProject(file, agName);
            parent.parent.println("agent "+agName+" ("+agFile+") was included in "+file);
        } catch(Exception e) {
            parent.parent.errorMsg("error adding agent:\n" + e);
        }
    }

    private void addAgInProject(File file, String agName) {
        // read and change
        var text = new StringBuilder();
        try (var in  = new BufferedReader(new FileReader(file) )) {
            String l = in.readLine();
            while (l != null) {
                if (l.contains("agents:"))
                    l = l.replace("agents: ", "agents: "+agName+";\n            ");
                text.append(l+"\n");
                l = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write
        try (var out = new BufferedWriter(new FileWriter(file))) {
            out.append(text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

