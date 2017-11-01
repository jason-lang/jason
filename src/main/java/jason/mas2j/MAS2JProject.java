package jason.mas2j;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.infra.InfrastructureFactory;
import jason.runtime.SourcePath;
import jason.util.Config;

/**
 * Represents a MAS2J project (usually created from a .mas2j file)
 */
public class MAS2JProject {

    public static final String EXT       = "mas2j";
    public static final String AS_EXT    = "asl";

    private static Logger logger = Logger.getLogger(MAS2JProject.class.getName());

    private String soc = "default";
    private ClassParameters envClass = null;
    private ClassParameters controlClass = null;
    private ClassParameters infrastructure = new ClassParameters("Centralised");
    private String projectDir = ".";
    private File   projectFile = null;
    private List<AgentParameters> agents = new ArrayList<AgentParameters>();
    private List<String> classpaths = new ArrayList<String>();
    private SourcePath aslSourcepaths = new SourcePath();
    private Map<String,String> directiveClasses = new HashMap<String,String>();

    public static MAS2JProject parse(String file) {
        try {
            jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new FileReader(file));
            return parser.mas();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing mas2j file.", e);
            return null;
        }
    }

    public void setupDefault() {
        if (envClass == null) {
            envClass = new ClassParameters(jason.environment.Environment.class.getName());
        }
    }

    public void setDirectory(String d) {
        if (d != null) {
            projectDir = d;
            if (projectDir.endsWith(File.separator) || projectDir.endsWith("/")) {
                projectDir = projectDir.substring(0,projectDir.length()-1);
            }
        }
    }

    public String getDirectory() {
        return projectDir;
    }

    public void setProjectFile(File f) {
        projectFile = f;
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setInfrastructure(ClassParameters infra) {
        infrastructure = infra;
    }
    public ClassParameters getInfrastructure() {
        return infrastructure;
    }

    public boolean isJade() {
        return getInfrastructure().getClassName().equals("Jade");
    }

    public void setEnvClass(ClassParameters e) {
        envClass = e;
    }
    public ClassParameters getEnvClass() {
        return envClass;
    }

    public void setSocName(String s) {
        soc = s;
    }

    public String getSocName() {
        return soc;
    }

    public void setControlClass(ClassParameters sControl) {
        controlClass = sControl;
    }
    public ClassParameters getControlClass() {
        return controlClass;
    }

    public void initAgMap() {
        agents = new ArrayList<AgentParameters>();
    }
    public void addAgent(AgentParameters a) {
        agents.add(a);
    }
    public AgentParameters getAg(String name) {
        for (AgentParameters a: agents) {
            if (a.name.equals(name)) {
                return a;
            }
        }
        return null;
    }

    public List<AgentParameters> getAgents() {
        return agents;
    }

    public Set<File> getAllASFiles() {
        Set<File> files = new HashSet<File>();
        for (AgentParameters agp: agents) {
            if (agp.asSource != null) {
                files.add(agp.asSource);
            }
        }
        return files;
    }

    /** change the source of the agents using the source path information,
     *  also considers code from a jar file (if urlPrefix is not null) */
    public void fixAgentsSrc() {
        for (AgentParameters agp: agents) {
            if (agp.asSource != null) {
                agp.asSource = new File(aslSourcepaths.fixPath(agp.asSource.toString()));
            }
        }
    }

    public void addClassPath(String cp) {
        if (cp.startsWith("\"")) {
            cp = cp.substring(1,cp.length()-1);
        }
        classpaths.add(cp);
    }

    public List<String> getClassPaths() {
        return classpaths;
    }

    public void addSourcePath(String cp) {
        aslSourcepaths.addPath(cp);
    }

    public SourcePath getSourcePaths() {
        return aslSourcepaths;
    }

    public void addDirectiveClass(String id, ClassParameters classname) {
        directiveClasses.put(id, classname.getClassName());
    }

    public Map<String,String> getDirectiveClasses() {
        return directiveClasses;
    }

    public void registerDirectives() {
        if (directiveClasses != null) {
            for (String id: directiveClasses.keySet()) {
                try {
                    DirectiveProcessor.registerDirective(id, Class.forName(directiveClasses.get(id)));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error registering directives "+directiveClasses,e);
                }
            }
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("/*\n");
        s.append("    Jason Project\n\n");
        s.append("    -- created on "+new SimpleDateFormat("MMMM dd, yyyy").format(new Date())+"\n");
        s.append("*/\n\n");
        s.append("MAS " + getSocName() + " {\n");
        s.append("   infrastructure: "+getInfrastructure()+"\n\n");

        if (getEnvClass() != null && ! getEnvClass().getClassName().equals(jason.environment.Environment.class.getName())) {
            s.append("   environment: "+getEnvClass());
            if (envClass.getHost() != null) {
                s.append(" at \""+envClass.getHost()+"\"");
            }
            s.append("\n\n");
        }

        if (getControlClass() != null) {
            s.append("   executionControl: "+getControlClass());
            if (getControlClass().getHost() != null) {
                s.append(" at \""+getControlClass().getHost()+"\"");
            }
            s.append("\n\n");
        }

        // agents
        s.append("   agents:\n");
        Iterator<AgentParameters> i = agents.iterator();
        while (i.hasNext()) {
            s.append("       "+i.next());
            s.append("\n");
        }
        s.append("\n");

        // directives
        if (directiveClasses.size() > 0) {
            s.append("   directives: ");
            for (String d: directiveClasses.keySet()) {
                s.append(d+"="+directiveClasses.get(d)+"; ");
            }
            s.append("\n");
        }

        // classpath
        if (classpaths.size() > 0) {
            s.append("   classpath: ");
            for (String cp: classpaths) {
                s.append("\""+cp+"\"; ");
            }
            s.append("\n");
        }

        // sourcepath
        if (!aslSourcepaths.isEmpty()) {
            s.append("   aslSourcePath: ");
            for (String cp: aslSourcepaths.getPaths()) {
                s.append("\""+cp+"\"; ");
            }
            s.append("\n");
        }

        s.append("}");

        return s.toString();
    }

    private InfrastructureFactory infraFac = null; // backup
    public InfrastructureFactory getInfrastructureFactory() throws JasonException {
        if (infraFac == null) {
            try {
                String facClass = Config.get().getInfrastructureFactoryClass(infrastructure.getClassName());
                infraFac = (InfrastructureFactory)Class.forName(facClass).newInstance();
            } catch (Exception e) {
                throw new JasonException("The project's infrastructure ('"+infrastructure.getClassName()+"') is not well configured! \n"+e);
            }
        }
        return infraFac;
    }
}
