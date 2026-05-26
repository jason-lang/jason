package jason.architecture.api.bootstrap;

import jason.architecture.MindInspectorWeb;
import jason.architecture.api.infrastructure.adapter.in.jason.AgentLogListener;
import jason.asSemantics.Agent;
import net.peelweb.PeelApp;
import net.peelweb.PeelAppBuilder;
import org.w3c.dom.Document;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class MindApiManager extends MindInspectorWeb {

    private static final Logger LOGGER = Logger.getLogger("Mind");

    private final MindApiModule module = new MindApiModule();

    private PeelApp app;

    private void initHandlersOnAgent(Agent agent) {
        AgentLogListener logListener = new AgentLogListener(agent, this.module.getAgentLogService());
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
                        this.module.agentController()).addController(this.module.masController())
                .addController(this.module.indexController()));
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
