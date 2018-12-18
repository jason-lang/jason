package jason.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** manages source paths and fixes absolute path for .asl */
public class SourcePath {
    public static final String CRPrefix = "ClassResource:";

    protected String       root = ".";
    protected String       urlPrefix = null;
    protected List<String> paths = new ArrayList<String>();

    public void setRoot(String r) {
        root = r;
    }
    public String getRoot() {
        return root;
    }

    public void setUrlPrefix(String p) {
        urlPrefix = p;
    }
    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void addPath(String cp) {
        if (cp.startsWith("\""))
            cp = cp.substring(1,cp.length()-1);
        if (cp.endsWith("/"))
        	cp = cp.substring(0,cp.length()-1);
        cp = cp.replaceAll("\\\\", "/"); // use unix path separator
        paths.add(cp);
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
        List<String> r = new ArrayList<String>();
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

    public String fixPath(String f) {
    	return fixPath(f, urlPrefix);
    }

    /** fix path of the asl code based on aslSourcePath, also considers code from a jar file (if urlPrefix is not null) */
    public String fixPath(String f, String urlPrefix) {
        if (urlPrefix == null || urlPrefix.length() == 0) {
            if (new File(f).exists()) {
                return f;
            } else {
                for (String path: getPaths()) {
                    try {
                        File newname = new File(path + "/" + f.toString());
                        if (newname.exists()) {
                            return newname.getCanonicalFile().toString();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (testURLSrc(urlPrefix + f)) {
                return urlPrefix + f;
            } else {
                for (String path: getPaths()) {
                    String newname = urlPrefix + path + "/" + f;
                    newname = newname.replaceAll("\\./", "");
                    if (testURLSrc(newname)) {
                        return newname;
                    }
                }
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
    	return urlPrefix + ":::" + this.root + " " + this.paths;
    }
}
