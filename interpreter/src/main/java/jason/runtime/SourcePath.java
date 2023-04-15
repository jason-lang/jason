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

    private boolean hasClassResource = false;

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
        if (!cp.startsWith("file:") && !cp.startsWith("jar:") && !cp.startsWith("http") && !cp.startsWith(CRPrefix))
        	cp = "file:" + cp;
        if (!paths.contains(cp)) {
	        paths.add(cp);

	        if (hasClassResource) {
        		if (cp.startsWith("file:")) {
        			paths.add(CRPrefix+"/"+cp.substring(5));
        		}
	        } else if (cp.startsWith(CRPrefix)) {
	        	hasClassResource = true;
	        	for (String p: new ArrayList<>(paths)) {
	        		if (p.startsWith("file:")) {
	        			paths.add(CRPrefix+"/"+p.substring(5));
	        		}
	        	}
	        }
        	//System.out.println(cp+" added to asl source path: " + paths);
        }
    }

    public void addParentInPath(String s) {
    	int p = s.indexOf(":");
    	if (p >= 0)
    		s = s.substring(p+1);

    	if (s.startsWith("/"))
    		s = s.substring(1);
    	File f = new File(s);
    	addPath(f.getParent());
    }

    public void addAll(SourcePath sp) {
    	if (sp == null)
    		return;
    	for (String p: sp.paths)
    		addPath(p);
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
    public String fixPath(String f) {
    	if (f==null)
    		return f;
    	if (f.isEmpty())
    		return f;
    	if (testURLSrc(f))
    		return f;
        if (new File(f).exists())
            return "file:"+f;
        if (f.startsWith("$")) { // the case of "$jason/src/a.asl"
            String jar = f.substring(1,f.indexOf("/"));
            if (Config.get().getPackage(jar) == null) {
            	System.err.println("The included file '"+jar+"' is not configured");
            } else {
                var nf = "jar:file:" + Config.get().getPackage(jar).getAbsolutePath() + "!" + f.substring(f.indexOf("/"));
                if (testURLSrc(nf))
                	return nf;
            }
        }
        String nf = f;
        if (nf.startsWith("file:"))
        	nf = f.substring(5);
        for (String path: getPaths()) {
        	String newname = path + "/" + nf;
        	newname = newname.replaceAll("\\./", "");
        	if (testURLSrc(newname)) {
        		//System.out.println(f+" fixed with "+newname);
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
