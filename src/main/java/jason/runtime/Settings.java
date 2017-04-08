package jason.runtime;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/** MAS Runtime Settings for an Agent (from mas2j file, agent declaration) */
public class Settings {

    public static final byte      ODiscard        = 1;
    public static final byte      ORequeue        = 2;
    public static final byte      ORetrieve       = 3;
    public static final boolean   OSameFocus      = true;
    public static final boolean   ONewFocus       = false;
    public static final int       ODefaultNRC     = 1;
    public static final int       ODefaultVerbose = -1;
    public static final boolean   ODefaultSync    = false;

    private static Logger logger = Logger.getLogger(Settings.class.getName());

    private byte    events     = ODiscard;
    private boolean intBels    = OSameFocus;
    private int     nrcbp      = ODefaultNRC;
    private int     verbose    = ODefaultVerbose;
    private boolean sync       = ODefaultSync;
    private boolean qCache     = false; // whether to use query cache
    private boolean qProfiling = false; // whether has query profiling
    private boolean troON      = true;  // tail recursion optimisation is on by default

    private Map<String,Object> userParameters = new HashMap<String,Object>();

    public static final String PROJECT_PARAMETER = "project-parameter";
    public static final String INIT_BELS  = "beliefs";
    public static final String INIT_GOALS = "goals";
    public static final String MIND_INSPECTOR = "mindinspector";
    public Settings() {
    }

    public Settings(String options) {
        setOptions(options);
    }

    @SuppressWarnings("unchecked")
    public void setOptions(String options) {
        logger.fine("Setting options from "+options);
        jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j( new StringReader(options));
        try {
            setOptions(parser.ASoptions());
            logger.fine("Settings are "+userParameters);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing options "+options,e);
        }
    }

    public void setOptions(Map<String,Object> options) {
        if (options == null) return;
        userParameters = options;

        for (String key: options.keySet()) {

            if (key.equals("events")) {
                String events = (String)options.get("events");
                if (events.equals("discard")) {
                    setEvents(ODiscard);
                } else if (events.equals("requeue")) {
                    setEvents(ORequeue);
                } else if (events.equals("retrieve")) {
                    setEvents(ORetrieve);
                }

            } else if (key.equals("intBels")) {
                String intBels = (String)options.get("intBels");
                if (intBels.equals("sameFocus")) {
                    setIntBels(OSameFocus);
                } else if (intBels.equals("newFocus")) {
                    setIntBels(ONewFocus);
                }

            } else if (key.equals("nrcbp")) {
                String nrc = (String)options.get("nrcbp");
                setNRCBP(nrc);

            } else if (key.equals("verbose")) {
                String verbose = (String)options.get("verbose");
                setVerbose(verbose);

            } else if (key.equals("synchronised")) {
                setSync("true".equals((String)options.get("synchronised")));
            } else if (key.equals("tro")) {
                setTRO("true".equals((String)options.get("tro")));
            } else if (key.equals("qcache")) {
                setQueryCache( "cycle".equals((String)options.get("qcache")) );
            } else if (key.equals("qprofiling")) {
                setQueryProfiling( "yes".equals((String)options.get("qprofiling")) );
            } else {
                //userParameters.put(key, options.get(key));
            }
        }
    }

    /** add user defined option */
    public void addOption(String key, Object value) {
        userParameters.put(key, value);
    }

    public void setEvents(byte opt) {
        events = opt;
    }

    public void setIntBels(boolean opt) {
        intBels = opt;
    }

    public void setNRCBP(String opt) {
        try {
            setNRCBP( Integer.parseInt(opt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNRCBP(int opt) {
        nrcbp = opt;
    }

    public void setVerbose(String opt) {
        try {
            setVerbose( Integer.parseInt(opt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVerbose(int opt) {
        verbose = opt;
    }

    public boolean discard() {
        return events == ODiscard;
    }

    public boolean requeue() {
        return events == ORequeue;
    }

    public boolean retrieve() {
        return events == ORetrieve;
    }

    public boolean sameFocus() {
        return(intBels);
    }
    public boolean newFocus() {
        return(!intBels);
    }

    public int nrcbp() {
        return nrcbp;
    }

    public int verbose() {
        return verbose;
    }

    public java.util.logging.Level logLevel() {
        switch(verbose) {
        case 0 :
            return java.util.logging.Level.WARNING;
        case 1 :
            return java.util.logging.Level.INFO;
        case 2 :
            return java.util.logging.Level.FINE;
        }
        return java.util.logging.Level.INFO;
    }

    /** returns true if the execution is synchronised */
    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean pSync) {
        sync = pSync;
    }

    public boolean isTROon() {
        return troON;
    }
    public void setTRO(boolean tro) {
        troON = tro;
    }

    public boolean hasQueryCache() {
        return qCache;
    }
    public void setQueryCache(boolean b) {
        qCache = b;
    }

    public boolean hasQueryProfiling() {
        return qProfiling;
    }
    public void setQueryProfiling(boolean b) {
        qProfiling = b;
    }

    public Map<String,Object> getUserParameters() {
        return userParameters;
    }

    public String getUserParameter(String key) {
        String vl = (String)userParameters.get(key);
        if (vl != null && vl.startsWith("\"") && vl.endsWith("\"")) {
            vl = vl.substring(1, vl.length()-1);
            vl = vl.replaceAll("\\\\\"", "\"");
        }
        return vl;
    }

    public Object removeUserParameter(String key) {
        return userParameters.remove(key);
    }

}
