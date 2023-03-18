package jason.architecture;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import org.w3c.dom.Document;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jason.asSemantics.Agent;
import jason.runtime.RuntimeServices;
import jason.runtime.Settings;
import jason.util.Config;
import jason.util.asl2html;
import jason.util.asl2xml;

public class MindInspectorWebImpl extends MindInspectorWeb {

    private HttpServer httpServer = null;

    private Map<String,List<Document>> histories = new TreeMap<>();
    private Map<String,Integer>        lastStepSeenByUser = new HashMap<>();
    private Map<String,Agent>          registeredAgents = new HashMap<>();

    private RuntimeServices            runner = null;

    public MindInspectorWebImpl() {
    }

    public synchronized String startHttpServer()  {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
            httpServer.setExecutor(Executors.newCachedThreadPool());

            httpServer.start();
            httpServerURL = "http://"+InetAddress.getLocalHost().getHostAddress()+":"+httpServerPort;
            System.out.println("Agent mind inspector is running at "+httpServerURL);
            registerRootBrowserView();
            registerAgentsBrowserView();
            registerAgView("no_ag");

            return httpServerURL;
        } catch (BindException e) {
            httpServerPort++;
            return startHttpServer();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void stoptHttpServer() {
        httpServer.stop(0);
        httpServer = null;
    }

    private void registerRootBrowserView() {
        if (httpServer == null)
            return;
        try {
            httpServer.createContext("/", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        String path = exchange.getRequestURI().getPath();
                        StringWriter so = new StringWriter();

                        if (path.length() < 2) { // it is the root
                            so.append("<html><head><title>Jason Mind Inspector -- Web View</title></head><body>");
                            so.append("<iframe width=\"20%\" height=\"100%\" align=left src=\"/agents\" border=5 frameborder=0 ></iframe>");
                            so.append("<iframe width=\"78%\" height=\"100%\" align=left src=\"/agent-mind/no_ag\" name=\"am\" border=5 frameborder=0></iframe>");
                            so.append("</body></html>");
                        } else if (path.indexOf("agent-mind") >= 0) {
                            if (tryToIncludeMindInspectorForAg(path))
                                so.append("<meta http-equiv=\"refresh\" content=0>");
                            else
                                so.append("unknown agent!");
                        }
                        responseBody.write(so.toString().getBytes());
                    }
                    responseBody.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAgNameFromPath(String path) {
        int nameStart = path.indexOf("agent-mind");
        if (nameStart < 0) return null;
        nameStart += "agent-mind".length() + 1;
        int nameEnd   = path.indexOf("/",nameStart+1);
        if (nameEnd >= 0)
            return path.substring(nameStart,nameEnd).trim();
        else
            return path.substring(nameStart).trim();
    }

    private boolean tryToIncludeMindInspectorForAg(String path) {
        try {
            Agent ag = registeredAgents.get(getAgNameFromPath(path));
            if (ag != null) {
                AgArch arch = ag.getTS().getAgArch();
                if (arch != null) {
                    // should add a new conf for mindinspector, otherwise will start a new gui for the agent
                    arch.getTS().getSettings().addOption(Settings.MIND_INSPECTOR,"web(1000,html,no_history)");
                    MindInspectorAgArch miArch = new MindInspectorAgArch();
                    arch.insertAgArch(miArch);
                    miArch.init();
                    miArch.addAgState();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void registerAgentsBrowserView() {
        if (httpServer == null)
            return;
        try {
            httpServer.createContext("/agents", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        responseBody.write(("<html><head><title>Jason (list of agents)</title></head><body>").getBytes());
                        responseBody.write(("<font size=\"+2\"><p style='color: red; font-family: arial;'>Agents</p></font>").getBytes());
                        var allAgs = histories.keySet();
                        for (String a: allAgs) {
                            responseBody.write( ("- <a href=\"/agent-mind/"+a+"/latest\" target=\"am\" style=\"font-family: arial; text-decoration: none\">"+a+"</a><br/>").getBytes());
                        }
                        if (runner != null) {
                            var wp = runner.getWP();
                            if (wp != null) {
                                for (String a: wp.keySet()) {
                                    if (! allAgs.contains(a)) {
                                        var uri = "";
                                        try {
                                            uri = wp.get(a).get("uri").toString();
                                        } catch (Exception e) {}
                                        responseBody.write( (". <a href=\""+uri+"\" target=\"am\" style=\"font-family: arial; text-decoration: none\">"+a+"</a><br/>").getBytes());
                                    }
                                }
                            }
                            if (!runner.getDF().isEmpty()) {
                                responseBody.write( ("<br/><a href=\"/df\" target=\"am\" style=\"font-family: arial; text-decoration: none\">DF</a><br/>").getBytes());
                            }
                        }
                    }
                    responseBody.write("<hr/>by <a href=\"http://jason.sf.net\" target=\"_blank\">Jason</a>".getBytes());
                    responseBody.write("</body></html>".getBytes());
                    responseBody.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** add the agent in the list of available agent for mind inspection */
    public synchronized void registerAg(Agent ag) {
        String agName = ag.getTS().getAgArch().getAgName();
        if (!agName.equals("no-named")) {
            registeredAgents.put(agName, ag);
            histories.put(agName, new ArrayList<Document>()); // just for the agent name to appear in the list of agents
        }
    }

    public synchronized void removeAg(Agent ag) {
        String agName = ag.getTS().getAgArch().getAgName();
        registeredAgents.remove(agName);
        histories.remove(agName);
    }

    public synchronized void addAgState(Agent ag, Document mind, boolean hasHistory) {
        String agName = ag.getTS().getAgArch().getAgName();
        List<Document> h = histories.get(agName);
        if (h == null) {
            h = new ArrayList<>();
            histories.put(agName, h);
        }
        if (h.isEmpty())
            registerAgView(agName); // the first time a state is added for the agent, register in the browser
        if (hasHistory || h.isEmpty())
            h.add(mind);
        else
            h.set(0, mind);
    }

    String registerAgView(final String agName) {
        if (httpServer == null)
            return null;
        try {
            String url = "/agent-mind/"+agName;
            httpServer.createContext(url, new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseHeaders.set("Content-Type", "text/html");

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        try {
                            StringWriter so = new StringWriter();
                            so.append("<html><head><title>"+agName+"</title>");

                            // test if the url is for this agent
                            String path = exchange.getRequestURI().getPath();
                            if (path.endsWith("/plans")) {
                                Agent ag = registeredAgents.get(agName);
                                if (ag != null) {
                                    so.append("<pre>");
                                    so.append(ag.getPL().getAsTxt(false));
                                    so.append("</pre>");
                                }
                            }  else {
                                if (!getAgNameFromPath(path).equals(agName)) {
                                    if (tryToIncludeMindInspectorForAg(path))
                                        so.append("<meta http-equiv=\"refresh\" content=0>");
                                    else
                                        so.append("unknown agent!");
                                } else {

                                    List<Document> h = histories.get(agName);
                                    if (h != null && h.size() > 0) {
                                        Document agState;
                                        int i = -1;
                                        exchange.getRemoteAddress();

                                        String query = exchange.getRequestURI().getRawQuery(); // what follows ?
                                        String remote = exchange.getRemoteAddress().toString();

                                        if (path.endsWith("hide")) {
                                            show.put(query, false);
                                            Integer ii = lastStepSeenByUser.get(remote);
                                            if (ii != null)
                                                i = ii;
                                        } else if (path.endsWith("show")) {
                                            show.put(query, true);
                                            Integer ii = lastStepSeenByUser.get(remote);
                                            if (ii != null)
                                                i = ii;
                                        } else if (path.endsWith("clear")) {
                                            agState = h.get(h.size() - 1);
                                            h.clear();
                                            h.add(agState);
                                        } else {
                                            // see if ends with a number
                                            try {
                                                int pos = path.lastIndexOf("/");
                                                String n = path.substring(pos + 1).trim();
                                                i = Integer.valueOf(n);
                                            } catch (Exception e) {
                                            }
                                        }
                                        if (i == -1) {
                                            //so.append("<meta http-equiv=\"refresh\" content=\""+refreshInterval+"\">");
                                            agState = h.get(h.size() - 1);
                                        } else {
                                            agState = h.get(i - 1);
                                        }
                                        try {
                                            lastStepSeenByUser.put(remote, i);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        so.append("</head><body>");
                                        if (h.size() > 1) {
                                            //so.append("history: ");
                                            so.append("<a href=/agent-mind/" + agName + "/latest>latest state</a> ");
                                            for (i = h.size() - 1; i > 0; i--) {
                                                so.append("<a href=\"/agent-mind/" + agName + "/" + i + "\" style=\"text-decoration: none\">" + i + "</a> ");
                                            }
                                            so.append("<a href=\"/agent-mind/" + agName + "/clear\">clear history</a> ");
                                            so.append("<hr/>");
                                        }
                                        so.append(getAgStateAsString(agState));
                                        if (show.get("annots")) {
                                            so.append("<hr/><a href='hide?annots'> hide annotations </a>");
                                        } else {
                                            so.append("<hr/><a href='show?annots'> show annotations </a>");
                                        }
                                        so.append("  |  <a href='plans'> plan library </a>");

                                        //so.append("<hr/><a href=\"/\"> list of agents</a> ");
                                    } else {
                                        so.append("select an agent");
                                    }
                                }
                            }
                            responseBody.write(so.toString().getBytes());

                            //responseBody.write(("<br/><a href=/agent-code/"+agName+">code</a>").getBytes());
                            responseBody.write("</body></html>".getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    responseBody.close();
                }
            });
            return httpServerURL+url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    String registerAgCodeBrowserView(String agId, final String agCode) {
        if (httpServer == null)
            return null;
        try {
            String url="/agent-code/"+agId;
            httpServer.createContext(url, new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        responseBody.write(agCode.getBytes());
                    }
                    responseBody.close();
                }
            });
            return httpServerURL+url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    */

    protected asl2xml   mindInspectorTransformer = null;
    Map<String,Boolean> show = new HashMap<>();

    synchronized String getAgStateAsString(Document ag) { // full means with show all
        try {
            if (mindInspectorTransformer == null) {
                mindInspectorTransformer = new asl2html("/xml/agInspection-nd.xsl");
                show.put("annots", Config.get().getBoolean(Config.SHOW_ANNOTS));
            }
            for (String p: show.keySet())
                mindInspectorTransformer.setParameter("show-"+p, show.get(p)+"");
            return mindInspectorTransformer.transform(ag); // transform to HTML
        } catch (Exception e) {
            e.printStackTrace();
            return "Error XML transformation (MindInspector)";
        }
    }

    public synchronized void registerCentRunner(RuntimeServices rs) {
        if (rs == null) return;

        this.runner = rs;
        httpServer.createContext("/df", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                String requestMethod = exchange.getRequestMethod();
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, 0);
                OutputStream responseBody = exchange.getResponseBody();

                if (requestMethod.equalsIgnoreCase("GET")) {
                    responseBody.write(("<html><head><title>Directory Facilitator State</title></head><body>").getBytes());
                    responseBody.write(("<font size=\"+2\"><p style='color: red; font-family: arial;'>Directory Facilitator State</p></font>").getBytes());

                    responseBody.write("<table border=\"0\" cellspacing=\"3\" cellpadding=\"6\" >".getBytes());
                    responseBody.write("<tr style='background-color: #ece7e6; font-family: arial;'><td><b>Agent</b></td><td><b>Services</b></td></tr>".getBytes());
                    Map<String, Set<String>> df = runner.getDF();
                    for (String a: df.keySet()) {
                        responseBody.write(("<tr style='font-family: arial;'><td>"+a+"</td>").getBytes());
                        for (String s: df.get(a)) {
                            responseBody.write(("<td>"+s+"<br/></td>").getBytes());
                        }
                        responseBody.write("</tr>".getBytes());

                    }
                    responseBody.write("</table>".getBytes());
                }
                responseBody.write("</body></html>".getBytes());
                responseBody.close();
            }
        });

    }

}
