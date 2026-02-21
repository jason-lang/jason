package jason.architecture.api.jason;

import jason.architecture.api.app.service.agent.StandardAgentService;
import jason.architecture.api.app.service.mas.StandardMasService;
import jason.architecture.api.app.state.AgentLogListener;
import jason.architecture.api.web.api.AgentController;
import jason.architecture.api.web.api.MasController;
import jason.architecture.api.web.view.IndexController;
import jason.architecture.MindInspectorWeb;
import jason.asSemantics.Agent;
import net.peelweb.PeelApp;
import net.peelweb.PeelAppBuilder;
import org.w3c.dom.Document;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class MindApiManager extends MindInspectorWeb {

    private static final Logger LOGGER = Logger.getLogger("Mind");

    private PeelApp app;

    private void initHandlersOnAgent(Agent agent) {
        AgentLogListener logListener = new AgentLogListener(agent);
        agent.getTS().getLogger().addHandler(logListener);
    }

    private void logAgentInitMessage(Agent agent) {
        String agentName = agent.getTS().getAgArch().getAgName();
        String endpoint;
        try {
            endpoint = String.format("http://%s:%s/mind/agents/%s", InetAddress.getLocalHost().getHostAddress(),
                    super.httpServerPort, agentName);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info(String.format("%s agent is in MAS and available at %s", agentName, endpoint));
    }

    @Override
    public String startHttpServer() {
        this.app = PeelAppBuilder.run(builder -> builder.port(super.httpServerPort).addController(
                        new AgentController(new StandardAgentService()))
                .addController(new MasController(new StandardMasService())).addController(new IndexController()));
        this.app.start();
        return "";
    }

    @Override
    public void stoptHttpServer() {
        this.app.stop();
    }

    @Override
    public void registerAg(Agent agent) {
        this.logAgentInitMessage(agent);
        this.initHandlersOnAgent(agent);
    }

    @Override
    public void removeAg(Agent agent) {

    }

    @Override
    public void addAgState(Agent agent, Document document, boolean b) {

    }

}
