package jason.infra.jade;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTerm;
import jason.asSyntax.parser.ParseException;
import jason.infra.MASLauncherInfraTier;
import jason.infra.centralised.CentralisedMASLauncherAnt;
import jason.mas2j.AgentParameters;
import jason.util.Config;

/**
 * Creates the script build.xml to launch the MAS using JADE.
 */
public class JadeMASLauncherAnt extends CentralisedMASLauncherAnt implements MASLauncherInfraTier {

    public static String snifferConfFile       = "sniffer.properties";
    public static String customSnifferConfFile = "c-sniffer.properties";
    //private static Logger logger = Logger.getLogger(JadeMASLauncherAnt.class.getName());

    protected String replaceMarks(String script, boolean debug) {
        // create sniffer file
        File sFile  = new File(project.getDirectory()+File.separator+snifferConfFile);
        File csFile = new File(project.getDirectory()+File.separator+customSnifferConfFile);
        try {
            if (csFile.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(csFile));
                BufferedWriter out = new BufferedWriter(new FileWriter(sFile));
                String line;
                while ( (line=in.readLine()) != null) {
                    out.write(line+"\n");
                }
                out.close();
                in.close();
            } else {
                sFile.delete();
                if (Config.get().getBoolean(Config.JADE_SNIFFER)) {
                    PrintWriter out = new PrintWriter(new FileWriter(sFile));
                    out.print("preload=");
                    Iterator<AgentParameters> i = project.getAgents().iterator();
                    while (i.hasNext()) {
                        AgentParameters ap = i.next();
                        out.print(ap.name);
                        if (i.hasNext()) out.print(";");
                    }
                    out.println();
                    out.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // replace build.xml tags
        String jadeJar = Config.get().getJadeJar();
        if (!Config.checkJar(jadeJar)) {
            System.err.println("The path to the jade.jar file (" + jadeJar + ") was not correctly set. Go to menu Plugin->Options->Jason to configure the path.");
        }

        script = replace(script, "<PROJECT-RUNNER-CLASS>", RunJadeMAS.class.getName());

        String jadepath = "\t<pathelement location=\"" + Config.get().getJadeJar() + "\"/>";
        /*try {
            String http = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/http.jar";
            jadepath += "\n\t<pathelement location=\"" + http + "\"/>";
        } catch (Exception _) {}
        try {
            String tools = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/jadeTools.jar";
            jadepath += "\n\t<pathelement location=\"" + tools + "\"/>";
        } catch (Exception _) {}
        try {
            String jar = new File(Config.get().getJadeJar()).getAbsoluteFile().getParent() + "/commons-codec-1.3.jar";
            jadepath += "\n\t<pathelement location=\"" + jar + "\"/>";
        } catch (Exception _) {}*/


        script = replace(script, "<PATH-LIB>", jadepath + "\n<PATH-LIB>");

        String startContainers = "";
        /*
           "    <target name=\"Main-Container\" depends=\"compile\" >\n"
         + "        <echo message=\"Starting JADE Main-Container\" />\n"
         + "        <java classname=\"jason.infra.jade.RunJadeMAS\" failonerror=\"true\" fork=\"yes\" dir=\"${basedir}\" >\n"
         + "            <classpath refid=\"project.classpath\"/>\n"
         + "            <arg line=\"${mas2j.project.file} -container-name Main-Container "+Config.get().getJadeArgs()+"\"/>\n"
         + "          <jvmarg line=\"-Xmx500M -Xss8M\"/>\n"
         + "        </java>\n"
         + "    </target>\n\n";
        */

        String mainHost;
        mainHost = project.getInfrastructure().getParameter("main_container_host");
        if (mainHost == null) {
            mainHost = "localhost";
        } else {
            try {
                mainHost = ((StringTerm)ASSyntax.parseLiteral(mainHost).getTerm(0)).getString();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        int mainHostPort = -1;
        int pos = mainHost.indexOf(":");
        if (pos > 0) {
            try {
                mainHostPort = new Integer(mainHost.substring(pos+1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainHost = mainHost.substring(0,pos);
        }


        // identify type of allocation (by class of info in .mas2h)
        ContainerAllocation allocator = null;
        String allocationClass = project.getInfrastructure().getParameter("container_allocation");
        if (allocationClass != null) {
            try {
                Literal literalArgs = ASSyntax.parseLiteral(allocationClass);
                String   className  = ((StringTerm)literalArgs.getTerm(0)).getString();

                //URLClassLoader loader = new URLClassLoader(new URL[] {
                //        new File(".").toURI().toURL(),
                //        new File("./bin/classes").toURI().toURL() });
                allocator = (ContainerAllocation)Class.forName(className).newInstance();
                allocator.init(new String[] { ((StringTerm)literalArgs.getTerm(1)).getString() }, project);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // collect containers in a set
        Set<String> containers = new HashSet<String>();
        containers.add("Main-Container");
        if (allocator == null) {
            for (AgentParameters ap: project.getAgents()) {
                if (ap.getHost() != null && !ap.getHost().isEmpty())
                    containers.add(ap.getHost());
            }
        } else {
            containers.addAll( allocator.getContainers() );
        }

        // script for the container's agents
        for (String container: containers) {
            String sep = " ";
            String args = "-container -host "+mainHost+" -container-name "+container+" ";
            if (mainHostPort > 0)
                args += "-port "+mainHostPort;

            StringBuilder agents = new StringBuilder();
            if (container.equals("Main-Container")) {
                // include environment (if not cartago)
                if (!JadeAgArch.isCartagoJadeCase(project) &&
                        project.getEnvClass() != null &&
                        !jason.environment.Environment.class.getName().equals(project.getEnvClass().getClassName())) {
                    agents.append(RunJadeMAS.environmentName+":"+JadeEnvironment.class.getName()+"(j-project,"+project.getProjectFile().getName()+")");
                    sep = ";";
                }
                args = "";
                args += Config.get().getJadeArgs();
                if (mainHostPort > 0)
                    args += " -port "+mainHostPort;
                if (Config.get().getBoolean(Config.JADE_RMA))
                    args += " -gui ";
            }
            for (AgentParameters ap: project.getAgents()) {
                for (int cAg = 0; cAg < ap.getNbInstances(); cAg++) {
                    String numberedAg = ap.getAgName();
                    if (ap.getNbInstances() > 1)
                        numberedAg += (cAg + 1);
                    if ( (container.equals("Main-Container") && ap.getHost() == null && allocator == null) ||
                            (ap.getHost() != null && ap.getHost().equals(container)) ||
                            (allocator != null && allocator.allocateAgent(numberedAg) != null && allocator.allocateAgent(numberedAg).equals(container))) {
                        agents.append(sep+numberedAg+":"+JadeAgArch.class.getName()+"(j-project,"+project.getProjectFile().getName()+","+ap.getAgName()+")");
                        sep = ";";
                    }
                }
            }
            if (agents.length() > 0)
                agents.insert(0, " -agents ");
            
            startContainers +=
                "    <target name=\""+container+"\" depends=\"compile\" >\n" +
                "        <echo message=\"Starting JADE Container "+container+"\" />\n"+
                "        <java classname=\"jade.Boot\" failonerror=\"true\" fork=\"yes\" dir=\"${basedir}\" >\n"+
                "            <classpath refid=\"project.classpath\"/>\n"+
                "            <arg line=\""+args+agents+"\"/>\n"+
                "            <jvmarg line=\"-Xmx500M -Xss8M\"/>\n"+
                "        </java>\n"+
                "    </target>\n\n";
        }

        script = replace(script, "<OTHER-TASK>", startContainers);

        return super.replaceMarks(script, debug);
    }
}
