package jason.mind.api.bootstrap;

import jason.mind.api.application.log.port.in.AgentLogService;
import jason.mind.api.application.log.service.StandardAgentLogService;
import jason.mind.api.application.mas.port.in.MasService;
import jason.mind.api.application.mas.service.StandardMasService;
import jason.mind.api.application.snapshot.port.in.AgentSnapshotService;
import jason.mind.api.application.snapshot.port.in.MessageSnapshotService;
import jason.mind.api.application.snapshot.service.StandardAgentSnapshotService;
import jason.mind.api.application.snapshot.service.StandardMessageSnapshotService;
import jason.mind.api.infrastructure.adapter.in.web.api.AgentStateController;
import jason.mind.api.infrastructure.adapter.in.web.api.MasController;
import jason.mind.api.infrastructure.adapter.in.web.view.IndexController;
import jason.mind.api.infrastructure.adapter.out.jason.DefaultJasonRuntimeGateway;
import jason.mind.api.infrastructure.adapter.out.memory.InMemoryMindRepository;
import lombok.Getter;

public class MindApiModule {

    @Getter
    private final AgentLogService agentLogService;

    private final MessageSnapshotService messageSnapshotService;

    private final AgentSnapshotService agentSnapshotService;

    private final MasService masService;

    public MindApiModule() {
        DefaultJasonRuntimeGateway defaultJasonRuntimeGateway = new DefaultJasonRuntimeGateway();

        this.agentLogService = new StandardAgentLogService(InMemoryMindRepository.getInstance());
        this.masService = new StandardMasService(defaultJasonRuntimeGateway);
        this.messageSnapshotService = new StandardMessageSnapshotService(InMemoryMindRepository.getInstance());
        this.agentSnapshotService = new StandardAgentSnapshotService(InMemoryMindRepository.getInstance());
    }

    public AgentStateController agentController() {
        return new AgentStateController(this.agentSnapshotService, this.messageSnapshotService, this.agentLogService);
    }

    public MasController masController() {
        return new MasController(this.masService, this.agentLogService);
    }

    public IndexController indexController() {
        return new IndexController();
    }
}
