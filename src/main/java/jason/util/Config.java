package jason.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.CentralisedFactory;
import jason.infra.jade.JadeFactory;

/**
 * Jason configuration (used by JasonID to generate the project's scripts)
 *
 * @author jomi
 */
public class Config extends Properties {

    private static final long  serialVersionUID = 1L;

    /** path to jason.jar */
    public static final String JASON_JAR     = "jasonJar";

    /** path to ant home (jar directory) */
    public static final String ANT_LIB       = "antLib";

    /** path to jade.jar */
    public static final String JADE_JAR      = "jadeJar";
    //public static final String MOISE_JAR     = "moiseJar";
    //public static final String JACAMO_JAR    = "jacamoJar";

    /** runtime jade arguments (the same used in jade.Boot) */
    public static final String JADE_ARGS     = "jadeArgs";

    /** boolean, whether to start jade RMA or not */
    public static final String JADE_RMA      = "jadeRMA";

    /** boolean, whether to start jade Sniffer or not */
    public static final String JADE_SNIFFER  = "jadeSniffer";

    /** path to java home */
    public static final String JAVA_HOME     = "javaHome";

    public static final String RUN_AS_THREAD = "runCentralisedInsideJIDE";
    public static final String SHELL_CMD     = "shellCommand";
    public static final String CLOSEALL      = "closeAllBeforeOpenMAS2J";
    public static final String CHECK_VERSION = "checkLatestVersion";
    public static final String WARN_SING_VAR = "warnSingletonVars";

    public static final String SHOW_ANNOTS   = "showAnnots";


    //public static final String jacamoHomeProp = "JaCaMoHome";

    public static final String SHORT_UNNAMED_VARS = "shortUnnamedVars";
    public static final String START_WEB_MI       = "startWebMindInspector";
    public static final String START_WEB_EI       = "startWebEnvInspector";
    public static final String START_WEB_OI       = "startWebOrgInspector";

    public static final String NB_TH_SCH      = "numberOfThreadsForScheduler";

    public static final String KQML_RECEIVED_FUNCTOR   = "kqmlReceivedFunctor";
    public static final String KQML_PLANS_FILE         = "kqmlPlansFile";

    protected static Config    singleton     = null;

    protected static String    configFactory = null;

    protected static boolean   showFixMsgs = true;
    
    public static void setClassFactory(String f) {
        singleton = null;
        configFactory = f;
    }

    public static Config get() {
        // return get(true);
        return get(false);
    }
    public static Config get(boolean tryToFixConfig) {
        if (singleton == null) {
            if (configFactory == null)
                configFactory = Config.class.getName();
            try {
                singleton = (Config)Class.forName(configFactory).newInstance();
            } catch (Exception e) {
                System.err.println("Error creating config from "+configFactory+"("+e+"), using default.");
                singleton = new Config();
            }
            if (!singleton.load()) {
                if (tryToFixConfig) {
                    singleton.fix();
                    singleton.store();
                }
            }
        }
        return singleton;
    }

    protected Config() {
    }

    public void setShowFixMsgs(boolean b) {
        showFixMsgs = b;        
    }
    
    /** returns the file where the user preferences are stored */
    public File getUserConfFile() {
        return new File(System.getProperties().get("user.home") + File.separator + ".jason/user.properties");
    }

    public File getMasterConfFile() {
        return new File("jason.properties");
    }

    public String getFileConfComment() {
        return "Jason user configuration";
    }

    /** Returns true if the file is loaded correctly */
    public boolean load() {
        try {
            File f = getUserConfFile();
            if (f.exists()) {
                super.load(new FileInputStream(f));
                return true;
            } else { // load master configuration file
                f = getMasterConfFile();
                if (f.exists()) {
                    System.out.println("User config file not found, loading master: "+f.getAbsolutePath());
                    super.load(new FileInputStream(f));
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading preferences");
            e.printStackTrace();
        }
        return false;
    }

    public boolean getBoolean(String key) {
        return "true".equals(get(key));
    }

    /** Returns the full path to the jason.jar file */
    public String getJasonJar() {
        return getProperty(JASON_JAR);
    }

    /** returns the jason home (based on jason.jar) */
    public String getJasonHome() {
        try {
            return new File(getJasonJar()).getParentFile().getParent();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return "";
    }

    /** Returns the full path to the jade.jar file */
    public String getJadeJar() {
        return getProperty(JADE_JAR);
    }

    /** Return the jade args (those used in jade.Boot) */
    public String getJadeArgs() {
        String a = getProperty(JADE_ARGS);
        return a == null ? "" : a;
    }

    public String[] getJadeArrayArgs() {
        List<String> ls = new ArrayList<String>();
        String jadeargs = getProperty(JADE_ARGS);
        if (jadeargs != null && jadeargs.length() > 0) {
            StringTokenizer t = new StringTokenizer(jadeargs);
            while (t.hasMoreTokens()) {
                ls.add(t.nextToken());
            }
        }
        String[] as = new String[ls.size()];
        for (int i=0; i<ls.size(); i++) {
            as[i] = ls.get(i);
        }
        return as;
    }

    /** Returns the path to the java  home directory */
    public String getJavaHome() {
        String h = getProperty(JAVA_HOME);
        if (! h.endsWith(File.separator))
            h += File.separator;
        return h;
    }

    /** Returns the path to the ant home directory (where its jars are stored) */
    public String getAntLib() {
        return getProperty(ANT_LIB);
    }

    public String getAntJar() {
        String ant = getAntLib();
        if (ant != null) {
            ant = findJarInDirectory(new File(ant), "ant-launcher");
            if (ant != null) {
                File fAnt = new File(ant);
                if (fAnt.exists())
                    return fAnt.getName();
            }
        }

        return null;
    }

    public void setJavaHome(String jh) {
        if (jh != null) {
            jh = new File(jh).getAbsolutePath();
            if (!jh.endsWith(File.separator)) {
                jh += File.separator;
            }
            put(JAVA_HOME, jh);
        }
    }

    public void setAntLib(String al) {
        if (al != null) {
            al = new File(al).getAbsolutePath();
            if (!al.endsWith(File.separator)) {
                al += File.separator;
            }
            put(ANT_LIB, al);
        }
    }

    public String getShellCommand() {
        return getProperty(SHELL_CMD);
    }

    public String getKqmlFunctor() {
        return getProperty(KQML_RECEIVED_FUNCTOR, Message.kqmlReceivedFunctor);
    }
    public String getKqmlPlansFile() {
        return getProperty(KQML_PLANS_FILE, Message.kqmlDefaultPlans);
    }

    public void resetSomeProps() {
        //System.out.println("Reseting configuration of "+Config.MOISE_JAR);
        //remove(Config.MOISE_JAR);
        //System.out.println("Reseting configuration of "+Config.JASON_JAR);
        remove(Config.JASON_JAR);
        //System.out.println("Reseting configuration of "+Config.JADE_JAR);
        remove(Config.JADE_JAR);
        //System.out.println("Reseting configuration of "+Config.ANT_LIB);
        remove(Config.ANT_LIB);
        put(Config.SHOW_ANNOTS, "false");
    }


    /** Set most important parameters with default values */
    public void fix() {
        tryToFixJarFileConf(JASON_JAR,  "jason",  700000);
        tryToFixJarFileConf(JADE_JAR,   "jade",  2000000);
        //tryToFixJarFileConf(MOISE_JAR,  "moise",  300000);
        //tryToFixJarFileConf(JACAMO_JAR, "jacamo",   5000);
        tryToFixJarFileConf(JASON_JAR,  "jason",  700000); // in case jacamo is found

        // fix java home
        if (get(JAVA_HOME) == null || !checkJavaHomePath(getProperty(JAVA_HOME))) {
            String javaHome = System.getProperty("java.home");
            if (checkJavaHomePath(javaHome)) {
                setJavaHome(javaHome);
            } else {
                String javaEnvHome = System.getenv("JAVA_HOME");
                if (javaEnvHome != null && checkJavaHomePath(javaEnvHome)) {
                    setJavaHome(javaEnvHome);
                } else {
                    String javaHomeUp = javaHome + File.separator + "..";
                    if (checkJavaHomePath(javaHomeUp)) {
                        setJavaHome(javaHomeUp);
                    } else {
                        // try JRE
                        if (checkJREHomePath(javaHome)) {
                            setJavaHome(javaHome);
                        } else {
                            setJavaHome(File.separator);
                        }
                    }
                }
            }
        }

        // fix ant lib
        if (get(ANT_LIB) == null || !checkAntLib(getAntLib())) {
            try {
                String jjar = getJasonJar();
                if (jjar != null) {
                    String antlib = new File(jjar).getParentFile().getParentFile().getAbsolutePath() + File.separator + "libs";
                    if (checkAntLib(antlib)) {
                        setAntLib(antlib);
                    } else {
                        antlib = new File(".") + File.separator + "libs";
                        if (checkAntLib(antlib)) {
                            setAntLib(antlib);
                        } else {
                            antlib = new File("..") + File.separator + "libs";
                            if (checkAntLib(antlib)) {
                                setAntLib(antlib);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error setting ant lib!");
                e.printStackTrace();
            }
        }

        // font
        if (get("font") == null) {
            put("font", "Monospaced");
        }
        if (get("fontSize") == null) {
            put("fontSize", "14");
        }

        // shell command
        if (get(SHELL_CMD) == null) {
            if (System.getProperty("os.name").startsWith("Windows 9")) {
                put(SHELL_CMD, "command.com /e:1024 /c ");
            } else if (System.getProperty("os.name").indexOf("indows") > 0) {
                put(SHELL_CMD, "cmd /c ");
            } else {
                put(SHELL_CMD, "/bin/sh ");
            }
        }

        // close all
        if (get(CLOSEALL) == null) {
            put(CLOSEALL, "true");
        }

        if (get(CHECK_VERSION) == null) {
            put(CHECK_VERSION, "true");
        }

        // jade args
        if (getProperty(JADE_RMA) == null) {
            put(JADE_RMA, "true");
        }

        // show annots
        if (getProperty(SHOW_ANNOTS) == null) {
            put(SHOW_ANNOTS, "true");
        }

        if (getProperty(START_WEB_MI) == null) {
            put(START_WEB_MI, "true");
        }

        if (getProperty(NB_TH_SCH) == null) {
            put(NB_TH_SCH, "2");
        }

        if (getProperty(SHORT_UNNAMED_VARS) == null) {
            put(SHORT_UNNAMED_VARS,"true");
        }

        if (getProperty(KQML_RECEIVED_FUNCTOR) == null) {
            put(KQML_RECEIVED_FUNCTOR, Message.kqmlReceivedFunctor);
        }

        if (getProperty(KQML_PLANS_FILE) == null) {
            put(KQML_PLANS_FILE, Message.kqmlDefaultPlans);
        }

        // Default infrastructures
        setDefaultInfra();
    }

    private void setDefaultInfra() {
        put("infrastructure.Centralised", CentralisedFactory.class.getName());
        put("infrastructure.Jade", JadeFactory.class.getName());
        //put("infrastructure.JaCaMo", "jacamo.infra.JaCaMoInfrastructureFactory");
    }

    public void store() {
        store(getUserConfFile());
    }

    public void store(File f) {
        try {
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            System.out.println("Storing configuration at "+f.getAbsolutePath());
            super.store(new FileOutputStream(f), getFileConfComment());
        } catch (Exception e) {
            System.err.println("Error writting preferences");
            e.printStackTrace();
        }
    }

    public String[] getAvailableInfrastructures() {
        try {
            List<String> infras = new ArrayList<String>();
            infras.add("Centralised"); // set Centralised as the first
            for (Object k: keySet()) {
                String sk = k.toString();
                int p = sk.indexOf(".");
                if (p > 0 && sk.startsWith("infrastructure") && p == sk.lastIndexOf(".")) { // only one "."
                    String newinfra = sk.substring(p+1);
                    if (!infras.contains(newinfra)) {
                        infras.add(newinfra);
                    }
                }
            }
            if (infras.size() > 0) {
                // copy infras to a array
                String[] r = new String[infras.size()];
                for (int i=0; i<r.length; i++) {
                    r[i] = infras.get(i);
                }
                return r;
            }
        } catch (Exception e) {
            System.err.println("Error getting user infrastructures.");
        }
        return new String[] {"Centralised","Jade" }; //,"JaCaMo"};
    }

    public String getInfrastructureFactoryClass(String infraId) {
        Object oClass = get("infrastructure." + infraId);
        if (oClass == null) {
            // try to fix using default configuration
            setDefaultInfra();
            oClass = get("infrastructure." + infraId);
        }
        return oClass.toString();
    }
    public void setInfrastructureFactoryClass(String infraId, String factory) {
        put("infrastructure." + infraId, factory);
    }
    public void removeInfrastructureFactoryClass(String infraId) {
        remove("infrastructure." + infraId);
    }


    /*public String getDistPropFile() {
        return "/dist.properties";
    }*/

    public String getJasonVersion() {
        Package j = Package.getPackage("jason.util");
        if (j != null) {
            return j.getSpecificationVersion();
        }
        return "?";
        /*
        try {
            Properties p = new Properties();
            p.load(Config.class.getResource(getDistPropFile()).openStream());
            String v = p.getProperty("version");
            if (v == null)
                v = "";
            else if (! v.isEmpty())
                v = v + ".";
            return  v + p.getProperty("release");
        } catch (Exception ex1) {
            try {
                Properties p = new Properties();
                //System.out.println("try 2 "+ex1);
                //ex1.printStackTrace();
                p.load(new FileReader("bin"+getDistPropFile()));
                return p.getProperty("version") + "." + p.getProperty("release");
            } catch (Exception ex2) {
                //System.out.println("*"+ex2);
                return "?";
            }
        }*/

    }

    public String getJasonBuiltDate() {
        Package j = Package.getPackage("jason.util");
        if (j != null) {
            return j.getImplementationVersion();
        }
        return "?";
        /*
        try {
            Properties p = new Properties();
            p.load(Config.class.getResource("/dist.properties").openStream());
            return p.get("build.date").toString();
        } catch (Exception ex) {
            return "?";
        }*/
    }

    public void tryToFixJarFileConf(String jarEntry, String jarFilePrefix, int minSize) {
        String jarFile = getProperty(jarEntry);
        if (jarFile == null || !checkJar(jarFile, minSize)) {
            if (showFixMsgs)
                System.out.println("Wrong configuration for " + jarFilePrefix + ", current is " + jarFile);

            // try eclipse installation
            jarFile = getJarFromEclipseInstallation(jarFilePrefix);
            if (checkJar(jarFile, minSize)) {
                put(jarEntry, jarFile);
                if (showFixMsgs)
                    System.out.println("found at " + jarFile+" in eclipse installation");
                return;
            }

            // try current dir
            jarFile = findJarInDirectory(new File("."), jarFilePrefix);
            if (checkJar(jarFile, minSize)) {
                try {
                    put(jarEntry, new File(jarFile).getCanonicalFile().getAbsolutePath());
                    if (showFixMsgs)
                        System.out.println("found at " + jarFile);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // try to get from classpath
            jarFile = getJarFromClassPath(jarFilePrefix);
            if (checkJar(jarFile, minSize)) {
                put(jarEntry, jarFile);
                if (showFixMsgs)
                    System.out.println("found at " + jarFile+" by classpath");
                return;
            }

            try {
                // try jason jar
                File jasonjardir = new File(getJasonJar()).getAbsoluteFile().getCanonicalFile().getParentFile();
                jarFile = findJarInDirectory(jasonjardir, jarFilePrefix);
                if (checkJar(jarFile, minSize)) {
                    put(jarEntry, jarFile);
                    if (showFixMsgs)
                        System.out.println("found at " + jarFile+" by jason.jar directory");
                    return;
                }
            } catch (Exception e) {}

            /*
            try {
                // try jacamo jar
                File jacamojardir= new File(getProperty(JACAMO_JAR)).getAbsoluteFile().getCanonicalFile().getParentFile();
                jarFile = findJarInDirectory(jacamojardir, jarFilePrefix);
                if (checkJar(jarFile, minSize)) {
                    put(jarEntry, jarFile);
                    System.out.println("found at " + jarFile+" by jacamo.jar directory");
                    return;
                }
            } catch (Exception e) {}
            */

            // try current dir + lib
            jarFile = findJarInDirectory(new File(".." + File.separator + "libs"), jarFilePrefix);
            if (checkJar(jarFile, minSize)) {
                try {
                    put(jarEntry, new File(jarFile).getCanonicalFile().getAbsolutePath());
                    if (showFixMsgs)
                        System.out.println("found at " + jarFile);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            jarFile = findJarInDirectory(new File("libs"), jarFilePrefix);
            if (checkJar(jarFile, minSize)) {
                try {
                    put(jarEntry, new File(jarFile).getCanonicalFile().getAbsolutePath());
                    if (showFixMsgs)
                        System.out.println("found at " + jarFile);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // try current dir + bin
            jarFile = findJarInDirectory(new File("bin"), jarFilePrefix);
            if (checkJar(jarFile, minSize)) {
                try {
                    put(jarEntry, new File(jarFile).getCanonicalFile().getAbsolutePath());
                    if (showFixMsgs)
                        System.out.println("found at " + jarFile);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // try from java web start
            String jwsDir = System.getProperty("jnlpx.deployment.user.home");
            if (jwsDir == null) {
                // try another property (windows)
                try {
                    jwsDir = System.getProperty("deployment.user.security.trusted.certs");
                    jwsDir = new File(jwsDir).getParentFile().getParent();
                } catch (Exception e) {
                }
            }
            if (jwsDir != null) {
                jarFile = findFile(new File(jwsDir), jarFilePrefix, minSize);
                if (showFixMsgs)
                    System.out.print("Searching " + jarFilePrefix + " in " + jwsDir + " ... ");
                if (jarFile != null && checkJar(jarFile)) {
                    if (showFixMsgs)
                        System.out.println("found at " + jarFile);
                    put(jarEntry, jarFile);
                    return;
                } else {
                    put(jarEntry, File.separator);
                }
            }
            if (showFixMsgs)
                System.out.println(jarFilePrefix+" not found");
        }

    }

    static String findFile(File p, String file, int minSize) {
        if (p.isDirectory()) {
            File[] files = p.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    String r = findFile(files[i], file, minSize);
                    if (r != null) {
                        return r;
                    }
                } else {
                    if (files[i].getName().endsWith(file) && files[i].length() > minSize) {
                        return files[i].getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    public static String findJarInDirectory(File dir, String prefix) {
        if (dir.isDirectory()) {
            for (File f: dir.listFiles()) {
                if (f.getName().startsWith(prefix) && f.getName().endsWith(".jar") && !f.getName().endsWith("-sources.jar") && !f.getName().endsWith("-javadoc.jar")) {
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public static boolean checkJar(String jar) {
        try {
            return jar != null && new File(jar).exists() && jar.endsWith(".jar");
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean checkJar(String jar, int minSize) {
        try {
            return checkJar(jar) && new File(jar).length() > minSize;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean checkJavaHomePath(String javaHome) {
        try {
            if (!javaHome.endsWith(File.separator)) {
                javaHome += File.separator;
            }
            File javac1 = new File(javaHome + "bin" + File.separatorChar + "javac");
            File javac2 = new File(javaHome + "bin" + File.separatorChar + "javac.exe");
            if (javac1.exists() || javac2.exists()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean checkJREHomePath(String javaHome) {
        try {
            if (!javaHome.endsWith(File.separator)) {
                javaHome += File.separator;
            }
            File javac1 = new File(javaHome + "bin" + File.separatorChar + "java");
            File javac2 = new File(javaHome + "bin" + File.separatorChar + "java.exe");
            if (javac1.exists() || javac2.exists()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean checkAntLib(String al) {
        try {
            if (!al.endsWith(File.separator)) {
                al = al + File.separator;
            }
            if (findJarInDirectory(new File(al), "ant") != null) // new File(al + "ant.jar");
                return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    static private String getJarFromClassPath(String file) {
        StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            File f = new File(token);
            if (f.getName().startsWith(file)  && f.getName().endsWith(".jar") && !f.getName().endsWith("-sources.jar") && !f.getName().endsWith("-javadoc.jar")) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    protected String getEclipseInstallationDirectory() {
        return "jason";
    }

    private String getJarFromEclipseInstallation(String file) {
        String eclipse = System.getProperty("eclipse.launcher");
        //eclipse = "/Applications/eclipse/eclipse";
        if (eclipse != null) {
            File f = (new File(eclipse)).getParentFile().getParentFile();
            if (eclipse.contains("Eclipse.app/Contents")) // MacOs case
                f = f.getParentFile().getParentFile();
            return findJarInDirectory(new File(f+"/"+getEclipseInstallationDirectory()+"/libs"), file);
        }
        return null;
    }

    public String getTemplate(String templateName) {
        try {
            if (templateName.equals("agent.asl"))
                templateName = "agent";
            if (templateName.equals("project.mas2j"))
                templateName = "project";

            String nl = System.getProperty("line.separator");
            // get template
            BufferedReader in;

            // if there is jason/src/xml/build-template.xml, use it; otherwise use the file in jason.jar
            File bt = new File("src/templates/"+templateName);
            if (bt.exists()) {
                in = new BufferedReader(new FileReader(bt));
            } else {
                bt = new File("../src/templates/"+templateName);
                if (bt.exists()) {
                    in = new BufferedReader(new FileReader(bt));
                } else {
                    bt = new File(getHome()+"/src/templates/"+templateName);
                    if (bt.exists()) {
                        in = new BufferedReader(new FileReader(bt));
                    } else {
                        bt = new File(getHome()+"/src/main/resources/templates/"+templateName);
                        if (bt.exists()) {
                            in = new BufferedReader(new FileReader(bt));
                        } else {
                            in = new BufferedReader(new InputStreamReader(getDetaultResource(templateName)));
                        }
                    }
                }
            }

            StringBuilder scriptBuf = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                scriptBuf.append(line + nl);
                line = in.readLine();
            }
            return scriptBuf.toString();
        } catch (Exception e) {
            System.err.println("Error reading template: " + templateName);
            e.printStackTrace();
            return null;
        }
    }

    protected String getHome() {
        return getJasonHome();
    }

    public InputStream getDetaultResource(String templateName) throws IOException {
        return TransitionSystem.class.getResource("/templates/"+templateName).openStream();
    }

    public static void main(String[] args) {
        Config.get().fix();
        Config.get().store();
    }

    public String getMindInspectorArchClassName() {
        return "jason.architecture.MindInspectorAgArch";
    }

    public String getMindInspectorWebServerClassName() {
        return "jason.architecture.MindInspectorWebImpl";
    }
    
    public String getPresentation() {
        return "Jason "+getJasonVersion()+"\n"+
               "     built on "+getJasonBuiltDate()+"\n"+
               "     installed at "+getHome();
    }

}
