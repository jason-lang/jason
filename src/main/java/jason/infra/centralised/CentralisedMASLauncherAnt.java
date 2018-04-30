package jason.infra.centralised;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import jason.infra.MASLauncherInfraTier;
import jason.infra.RunProjectListener;
import jason.mas2j.MAS2JProject;
import jason.util.Config;

/**
 * Write the Ant script to run the MAS in centralised infrastructure and
 * start this script.
 *
 * @author Jomi
 */
public class CentralisedMASLauncherAnt implements MASLauncherInfraTier {

    protected MAS2JProject       project;
    protected RunProjectListener listener;
    protected boolean            stop       = false;
    protected Process            masProcess = null;
    protected OutputStream       processOut;

    protected boolean            useBuildFileName = true;

    public static String         bindir = "bin"+File.separator;

    private String task;

    public CentralisedMASLauncherAnt() {
        task = "run";
    }

    /** create the launcher for a specific ant task */
    public CentralisedMASLauncherAnt(String task) {
        this.task = task;
    }

    public void setProject(MAS2JProject project) {
        this.project = project;
    }

    public void setListener(RunProjectListener listener) {
        this.listener = listener;
    }

    public void setTask(String t) {
        task = t;
    }

    public void run() {
        try {
            String[] command = getStartCommandArray();

            if (command == null) {
                System.err.println("Problem defining the command to run the MAS!");
                return;
            }

            String cmdstr = command[0];
            for (int i = 1; i < command.length; i++) {
                cmdstr += " " + command[i];
            }


            System.out.println("Executing " + cmdstr);

            File dir = new File(new File(project.getDirectory()).getAbsolutePath());
            masProcess = Runtime.getRuntime().exec(command, null, dir);

            BufferedReader in = new BufferedReader(new InputStreamReader(masProcess.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(masProcess.getErrorStream()));
            processOut = masProcess.getOutputStream();

            Thread.sleep(300);
            stop = false;
            // read the program output and print it out
            while (!stop) {
                while (in.ready()) {
                    System.out.println(in.readLine());
                }
                while (err.ready()) {
                    System.out.println(err.readLine());
                }
                Thread.sleep(250); // to not consume cpu

                try {
                    masProcess.exitValue();
                    // no exception when the process has finished
                    stop = true;
                } catch (Exception e) {
                }
            }
            while (in.ready()) {
                System.out.println(in.readLine());
            }
            while (err.ready()) {
                System.out.println(err.readLine());
            }
            System.out.flush();
        } catch (Exception e) {
            System.err.println("Execution error: " + e);
            e.printStackTrace();
        } finally {
            if (listener != null) {
                listener.masFinished();
            }
        }
    }

    public void stopMAS() {
        try {
            // creating this file will stop the MAS, the runner checks for this file creation
            File stop = new File(project.getDirectory()+File.separator+BaseCentralisedMAS.stopMASFileName);
            stop.createNewFile();
        } catch (Exception e) {
            System.err.println("Error stoping RunCentMAS: " + e);
            e.printStackTrace();
        } finally {
            stop = true;
        }
    }

    /** returns the operating system command that runs the MAS */
    public String[] getStartCommandArray() {
        String build = bindir+getBuildFileName();;
        if (hasCBuild()) build = bindir+getCustomBuildFileName();
        String ant = Config.get().getAntJar();
        if (ant == null) {
            System.err.println("Ant is not properly configured! Current value is "+Config.get().getAntLib());
            return null;
        } else {
            return new String[] { Config.get().getJavaHome() + "bin" + File.separator + "java",
                                  "-classpath",
                                  Config.get().getAntLib() + ant, "org.apache.tools.ant.launch.Launcher",
                                  "-e", "-f", build, task
                                };
        }
    }

    public String getBuildFileName() {
        if (useBuildFileName) {
            return "build.xml";
        } else {
            return project.getSocName()+".xml";
        }
    }
    public String getCustomBuildFileName() {
        if (useBuildFileName) {
            return "c-build.xml";
        } else {
            return "c-"+project.getSocName()+".xml";
        }
    }

    /** write the scripts necessary to run the project */
    public boolean writeScripts(boolean debug, boolean useBuildFileName) {
        this.useBuildFileName = useBuildFileName;

        //if (hasCBuild()) {
        //    System.out.println("The build.xml file is not being created, the user c-build.xml file is used instead.");
        //    return true; // if the user has a c-build.xml file, use his build
        //}

        try {
            String script = Config.get().getTemplate("build-template.xml");

            // replace <....>
            script = replaceMarks(script, debug);

            // create bin dir
            File bindirfile = new File(project.getDirectory()+File.separator+bindir);
            if (!bindirfile.exists()) {
                bindirfile.mkdirs();
            }

            // write the script
            FileWriter out = new FileWriter(project.getDirectory() + File.separator + bindir + getBuildFileName());
            out.write(script);
            out.close();
            return true;
        } catch (Exception e) {
            System.err.println("Could not write start script for project " + project.getSocName());
            e.printStackTrace();
            return false;
        }
    }

    protected String replaceMarks(String script, boolean debug) {
        script = replace(script, "<VERSION>", Config.get().getJasonVersion());
        script = replace(script, "<DATE>", new SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss").format(new Date()));
        script = replace(script, "<PROJECT-ID>", project.getSocName());

        String dDir = project.getDirectory();
        if (dDir.endsWith(File.separator + ".")) {
            dDir = dDir.substring(0, dDir.length() - 2);
        }
        if (dDir.endsWith(File.separator)) {
            dDir = dDir.substring(0, dDir.length() - 1);
        }
        script = replace(script, "<PROJECT-DIR>", dDir);
        script = replace(script, "<PROJECT-FILE>", project.getProjectFile().getName());

        script = replace(script, "<JASON-JAR>", Config.get().getJasonJar());

        // add lib/*.jar
        String lib = "";

        // if cartago env
        if (project.isJade() ||
                (project.getEnvClass() != null && project.getEnvClass().getClassName().equals("jaca.CartagoEnvironment"))) {
            Config c = Config.get();
            String cartago = Config.findJarInDirectory(new File(c.getJasonHome()+"/libs"), "cartago");
            if (cartago != null)
                lib += "        <pathelement location=\""+cartago+"\"/>\n";
            String c4jason = Config.findJarInDirectory(new File(c.getJasonHome()+"/libs"), "jaca");
            if (c4jason != null)
                lib += "        <pathelement location=\""+c4jason+"\"/>\n";
        }

        if (new File(dDir + File.separator + "lib").exists()) {
            lib += "        <fileset dir=\"${basedir}/lib\" >  <include name=\"*.jar\" /> </fileset>\n";
        }
        if (new File(dDir + File.separator + "libs").exists()) {
            lib += "        <fileset dir=\"${basedir}/libs\" >  <include name=\"*.jar\" /> </fileset>\n";
        }

        // add classpath defined in the project .mas2j
        for (String cp: project.getClassPaths()) {
            int apos = cp.indexOf("*");
            if (apos < 0) {
                lib += "        <pathelement location=\""+cp+"\"/>\n";
            } else {
                cp = cp.replaceAll("\\\\", "/");
                String dir   = "${basedir}";
                String files = cp;
                int spos = cp.lastIndexOf("/");
                if (spos >= 0 && spos < apos) {
                    dir   = cp.substring(0,spos);
                    files = cp.substring(spos+1);
                } else {
                    spos = cp.lastIndexOf("/**");
                    if (spos >= 0 && spos < apos) {
                        dir   = cp.substring(0,spos);
                        files = cp.substring(spos+1);
                    }
                }

                lib += "        <fileset dir=\""+dir+"\" >  <include name=\""+files+"\" /> </fileset>\n";
            }
        }

        script = replace(script, "<PATH-LIB>", lib);

        script = replace(script, "<PROJECT-RUNNER-CLASS>", jason.infra.centralised.RunCentralisedMAS.class.getName());
        String sDebug = "";
        if (debug) {
            sDebug = " -debug";
        }
        script = replace(script, "<DEBUG>", sDebug);
        script = replace(script, "<OTHER-TASK>", "");
        script = replace(script, "<JASON-HOME>", Config.get().getJasonHome());
        script = replace(script, "<JASON-HOME>", Config.get().getJasonHome());
        script = replace(script, "<RUN-ARGS>", "");

        return script;
    }

    public static String replace(String s, String p, String n) {
        int i = s.indexOf(p);
        if (i >= 0) {
            s = s.substring(0, i) + n + s.substring(i + p.length());
        }
        return s;
    }

    protected boolean hasCBuild() {
        return new File(project.getDirectory() + File.separator + bindir + getCustomBuildFileName()).exists();
    }

}
