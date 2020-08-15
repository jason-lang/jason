package jason.runtime;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jason.util.Config;

/** manages source paths and fixes absolute path for .asl */
public class SourcePath implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String CRPrefix = "ClassResource:";

    protected String       root = ".";
    protected List<String> paths = new ArrayList<>();

    public void setRoot(String r) {
        root = r;
    }
    public String getRoot() {
        return root;
    }

    public void addPath(String cp) {
        if (cp == null)
            return;
        if (cp.isEmpty())
            return;

        if (cp.startsWith("\""))
            cp = cp.substring(1,cp.length()-1);
        if (cp.endsWith("/"))
        	cp = cp.substring(0,cp.length()-1);
        if (cp.startsWith("$"))
        	cp = fixPath(cp);
        cp = cp.replaceAll("\\\\", "/"); // use unix path separator
        if (!cp.startsWith("jar:") && !cp.startsWith("http") && !cp.startsWith(CRPrefix))
        	cp = "file:" + cp;
        if (!paths.contains(cp)) {
	        System.out.println("added "+cp);
	        paths.add(cp);
        }
    }

    public void addAll(SourcePath sp) {
    	if (sp == null)
    		return;
    	for (String p: sp.paths)
    		paths.add(p);
    }

    public void clearPaths() {
    	paths.clear();
    }

    public List<String> getPaths() {
        List<String> r = new ArrayList<>();
        if (paths.isEmpty()) {
            r.add(root);
        } else {
	        for (String p: paths) {
	            r.add(p);
	            if ( !p.startsWith(".") && !p.startsWith("/") && p.charAt(1) != ':' && !root.equals(".")) {
	                // try both, with and without the current directory
	                r.add(root+"/"+p);
	            }
	        }
        }
        return r;
    }

    public boolean isEmpty() {
    	return paths.isEmpty();
    }

    /** fix path of the asl code based on aslSourcePath, also considers code from a jar file (if urlPrefix is not null) */
    public String fixPath(String f) { //, String urlPrefix) {
    	if (f==null)
    		return f;
    	if (f.isEmpty())
    		return f;
        if (new File(f).exists()) {
            return f;
        } else {
            if (f.startsWith("$")) { // the case of "$jasonJar/src/a.asl"
                String jar = f.substring(1,f.indexOf("/"));
                if (Config.get().get(jar) == null) {
                	System.err.println("The included file '"+jar+"' is not configured");
                } else {
                    String path = Config.get().get(jar).toString();
                    String nf = "jar:file:" + path + "!" + f.substring(f.indexOf("/"));
                    if (testURLSrc(nf))
                    	return nf;
                }
            }
            for (String path: getPaths()) {
            	String newname = path + "/" + f;
            	newname = newname.replaceAll("\\./", "");
            	if (testURLSrc(newname))
            		return newname;
            }
        }
        return f;
    }

    private static boolean testURLSrc(String asSrc) {
        try {
            if (asSrc.startsWith(CRPrefix)) {
            	SourcePath.class.getResource(asSrc.substring(CRPrefix.length())).openStream();
                return true;
            } else {
                // Agent.class.getResource(asSrc).openStream();
                new URL(asSrc).openStream();
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    @Override
    public String toString() {
    	return this.root + " " + this.paths;
    }
}
