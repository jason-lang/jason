package jason.util;

import java.io.*;
import java.net.URL;
import java.util.*;

import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;

/**
 * Jason configuration
 *
 * @author jomi
 */
public class Config extends Properties {

    @Serial
    private static final long  serialVersionUID = 1L;

    /** path to jason.jar */
    public static final String JASON_JAR     = "jasonJar";
    public static final String JASON_PKG     = "jason";

    /** runtime jade arguments (the same used in jade.Boot) */
    public static final String JADE_ARGS     = "jadeArgs";

    /** boolean, whether to start jade RMA or not */
    public static final String JADE_RMA      = "jadeRMA";

    /** boolean, whether to start jade Sniffer or not */
    public static final String JADE_SNIFFER  = "jadeSniffer";

    public static final String WARN_SING_VAR = "warnSingletonVars";

    public static final String SHOW_ANNOTS   = "showAnnots";

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

    protected Map<String,File> packages = new HashMap<>(); // a map from 'jason' -> 'jar:file:/..../jason.jar' and other packages

    public static void setClassFactory(String f) {
        singleton = null;
        configFactory = f;
    }

    public static Config get() {
        return get(false);
    }
    public static Config get(boolean tryToFixConfig) {
        if (singleton == null) {
            if (configFactory == null)
                configFactory = Config.class.getName();
            try {
                singleton = (Config)Class.forName(configFactory).getConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("Error creating config from "+configFactory+" ("+e+"), using default.");
                singleton = new Config();
            }
            if (!singleton.load()) {
                if (tryToFixConfig) {
                    singleton.fix();
                    //singleton.store();
                }
            }
        }
        return singleton;
    }

    public Config() {
    }

    public void setShowFixMsgs(boolean b) {
        showFixMsgs = b;
    }

    /** returns the file where the user preferences are stored */
    public File getUserConfFile() {
        return new File(System.getProperties().get("user.home") + File.separator + ".jason/user.properties");
    }

    public File getLocalConfFile() {
        return new File("jason.properties");
    }

    public String getFileConfComment() {
        return "Jason user configuration";
    }

    /** Returns true if the file is loaded correctly */
    public boolean load() {
        try {
            File f = getLocalConfFile();
            if (f.exists()) {
                super.load(new FileInputStream(f));
                return true;
            } else {
                f = getUserConfFile();
                if (f.exists()) {
                    //System.out.println("User config file not found, loading: "+f.getAbsolutePath());
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

    @Override
    public synchronized Object put(Object key, Object value) {
        if (JASON_JAR.equals(key)) {
            addPackage(JASON_PKG, new File((String)value));
            addPackage(JASON_JAR, new File((String)value)); // for compatibility reasons
        }
        return super.put(key, value);
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

    /** Return the jade args (those used in jade.Boot) */
    public String getJadeArgs() {
        String a = getProperty(JADE_ARGS);
        return a == null ? "" : a;
    }

    public String[] getJadeArrayArgs() {
        List<String> ls = new ArrayList<>();
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

    public String getKqmlFunctor() {
        return getProperty(KQML_RECEIVED_FUNCTOR, Message.kqmlReceivedFunctor);
    }
    public String getKqmlPlansFile() {
        return getProperty(KQML_PLANS_FILE, Message.kqmlDefaultPlans);
    }

    /** Set most important parameters with default values */
    public void fix() {
        tryToFixJarFileConf(JASON_JAR,  "jason");

        // check inconsistencies for jason.jar
        String jasonJarFile = getJarFromClassPath("jason", getJarFileForFixTest(JASON_JAR));
        if (checkJar(jasonJarFile, getJarFileForFixTest(JASON_JAR))) {
            if (getJasonJar() != null && !getJasonJar().equals(jasonJarFile)) {
                System.out.println("\n\n*** The jason.jar from classpath is different than jason.jar from configuration, consider to delete the configuration (file ~/.jason/user.properties) or 'unset JASON_HOME'.");
                System.out.println("Classpath is\n   "+jasonJarFile+
                        "\nConfig    is\n   "+getJasonJar()+"\n\n");
                System.out.println("Using the jason.jar from classpath\n");
            }
            put(JASON_JAR, jasonJarFile); // always prefer classpath jar
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

    public String getJasonVersion() {
        //Package j = jason.util.ConfigGUI.class.getClassLoader().getDefinedPackage("jason.util");
        Package j = Package.getPackage("jason.util");
        if (j != null && j.getSpecificationVersion() != null) {
            return j.getSpecificationVersion();
        }

        return "undefined version";
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
        //Package j = jason.util.ConfigGUI.class.getClassLoader().getDefinedPackage("jason.util");
        Package j = Package.getPackage("jason.util");
        if (j != null) {
            return j.getImplementationVersion();
        }
        return "undefined build";
        /*
        try {
            Properties p = new Properties();
            p.load(Config.class.getResource("/dist.properties").openStream());
            return p.get("build.date").toString();
        } catch (Exception ex) {
            return "?";
        }*/
    }

    @SuppressWarnings("rawtypes")
    public Class getClassForClassLoaderTest(String jarEntry) {
        if (jarEntry == JASON_JAR)
            return TransitionSystem.class;
        return this.getClass();
    }

    public String getJarFileForFixTest(String jarEntry) {
        if (jarEntry == JASON_JAR)
            return "jason/asSyntax/CyclicTerm.class";
        return null;
    }


    public boolean tryToFixJarFileConf(String jarEntry, String jarFileNamePrefix) {
        String jarFile   = getProperty(jarEntry);
        String fileInJar = getJarFileForFixTest(jarEntry);
        if (jarFile == null || !checkJar(jarFile, fileInJar)) {
            //if (showFixMsgs)
            //    System.out.println("Wrong configuration for " + jarFileNamePrefix + ", current is " + jarFile);

            // try to get by class loader
            try {
                String fromLoader = getClassForClassLoaderTest(jarEntry).getProtectionDomain().getCodeSource().getLocation().toString();
                if (fromLoader.startsWith("file:"))
                    fromLoader = fromLoader.substring(5);
                if (new File(fromLoader).getName().startsWith(jarFileNamePrefix) && checkJar(fromLoader, fileInJar)) {
                    if (showFixMsgs)
                        System.out.println("Configuration of '"+jarEntry+"' found at " + fromLoader+", based on class loader");
                    put(jarEntry, fromLoader);
                    return true;
                }
            } catch (Exception e) {}

            // try to get from classpath (the most common case)
            jarFile = getJarFromClassPath(jarFileNamePrefix, fileInJar);
            if (checkJar(jarFile, fileInJar)) {
                put(jarEntry, jarFile);
                if (showFixMsgs)
                    System.out.println("Configuration of '"+jarEntry+"' found at " + jarFile+", based on classpath");
                return true;
            }
            if (showFixMsgs)
                System.out.println("Configuration of '"+jarEntry+"' NOT found, based on class loader");

            // try with $JASON_HOME
            String jh = System.getenv().get("JASON_HOME");
            if (jh != null) {
                jarFile = findJarInDirectory(new File(jh+"/interpreter/build/libs"), jarFileNamePrefix);
                if (checkJar(jarFile, fileInJar)) {
                    try {
                        put(jarEntry, new File(jarFile).getCanonicalFile().getAbsolutePath());
                        if (showFixMsgs)
                            System.out.println("Configuration of '"+jarEntry+"' found at " + jarFile + ", based on JASON_HOME");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (showFixMsgs)
                System.out.println("Configuration of '"+jarEntry+"' NOT found, based on JASON_HOME="+jh);

            // try current build/libs (from gradle build), required for task testJason
            var localBuild = new File("build/libs").getAbsoluteFile();
            jarFile = findJarInDirectory( localBuild, jarFileNamePrefix);
            if (checkJar(jarFile, fileInJar)) {
                try {
                    put(jarEntry, new File(jarFile).getCanonicalFile().getAbsolutePath());
                    if (showFixMsgs)
                        System.out.println("Configuration of '"+jarEntry+"' found at " + jarFile);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (showFixMsgs)
                System.out.println("Configuration of '"+jarEntry+"' NOT found in "+localBuild);

            return false;
        }
        return true;
    }

    static String findFile(File p, String file) {
        if (p.isDirectory()) {
            File[] files = p.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    String r = findFile(f, file);
                    if (r != null) {
                        return r;
                    }
                } else {
                    if (f.getName().endsWith(file)) {
                        return f.getAbsolutePath();
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

    public boolean checkJar(String jar, String file) {
        try {
            return checkJar(jar) && checkJarHasFile(jar,file);
        } catch (Exception e) {
        }
        return false;
    }

    public boolean checkJarHasFile(String jarFile, String file) {
        if (file == null || jarFile == null)
            return true;

        jarFile = "jar:file:" + jarFile + "!/" + file;
        try {
            new URL(jarFile).openStream().close();
            return true;
        } catch (Exception e) { }
        return false;
    }

    protected String getJarFromClassPath(String file, String fileInsideJar) {
        StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            File f = new File(token);
            if (f.getName().startsWith(file) &&
                    f.getName().endsWith(".jar") &&
                    checkJarHasFile(f.getAbsolutePath(), fileInsideJar)) {
                return f.getAbsolutePath();
            }
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
                            in = new BufferedReader(new InputStreamReader(getDefaultResource(templateName)));
                        }
                    }
                }
            }

            StringBuilder scriptBuf = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                scriptBuf.append(line).append(nl);
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

    public InputStream getDefaultResource(String templateName) throws IOException {
        return TransitionSystem.class.getResource("/templates/"+templateName).openStream();
    }

    public static void main(String[] args) {
        showFixMsgs = true;
        Config.get(true);
    }

    public String getMindInspectorArchClassName() {
        return "jason.architecture.MindInspectorAgArch";
    }

    public String getMindInspectorWebServerClassName() {
        return "jason.architecture.MindInspectorWebImpl";
    }

    public String getPresentation() {
        return "Jason "+getJasonVersion()+"\n"+
               "     built on "+getJasonBuiltDate();
    }

    public void addPackage(String key, File value) {
        packages.put(key, value);
    }

    public File getPackage(String key) {
        return packages.get(key);
    }
    public Map<String, File> getPackages() {
        return packages;
    }

    public void clearPackages() {
        packages.clear();
    }

}
