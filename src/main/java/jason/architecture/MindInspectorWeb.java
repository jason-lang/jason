package jason.architecture;

import org.w3c.dom.Document;

import jason.asSemantics.Agent;
import jason.util.Config;

public abstract class MindInspectorWeb {

    private static MindInspectorWeb singleton = null;

    protected String     httpServerURL = "http://localhost:3272";
    protected int        httpServerPort = 3272;
    protected int        refreshInterval = 5;

    public static synchronized MindInspectorWeb get() {
        if (singleton == null) {
            try {
                singleton = (MindInspectorWeb) Class.forName( Config.get().getMindInspectorWebServerClassName()).newInstance();
                singleton.startHttpServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return singleton;
    }
    
    public static synchronized void stop() {
        if (singleton != null) {
            singleton.stoptHttpServer();
            singleton = null;
        }
    }

    public static boolean isRunning() {
        return singleton != null;
    }

    public static String getURL() {
        if (singleton != null)
            return singleton.httpServerURL;
        else
            return null;
    }

    public abstract String startHttpServer();
    public abstract void   stoptHttpServer();

    /** add the agent in the list of available agent for mind inspection */
    public abstract void registerAg(Agent ag);

    public abstract void removeAg(Agent ag);
    public abstract void addAgState(Agent ag, Document mind, boolean hasHistory);

}
