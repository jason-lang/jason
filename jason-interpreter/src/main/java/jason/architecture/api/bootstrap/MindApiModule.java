package jason.architecture.api.bootstrap;

import jason.architecture.api.application.log.port.in.AgentLogService;
import jason.architecture.api.application.log.service.StandardAgentLogService;
import jason.architecture.api.application.mas.port.in.MasService;
import jason.architecture.api.application.mas.service.StandardMasService;
import jason.architecture.api.application.snapshot.port.in.AgentSnapshotService;
import jason.architecture.api.application.snapshot.port.in.MessageSnapshotService;
import jason.architecture.api.application.snapshot.service.StandardAgentSnapshotService;
import jason.architecture.api.application.snapshot.service.StandardMessageSnapshotService;
import jason.architecture.api.infrastructure.adapter.in.web.api.AgentStateController;
import jason.architecture.api.infrastructure.adapter.in.web.api.MasController;
import jason.architecture.api.infrastructure.adapter.in.web.view.IndexController;
import jason.architecture.api.infrastructure.adapter.out.jason.DefaultJasonRuntimeGateway;
import jason.architecture.api.infrastructure.adapter.out.memory.InMemoryMindRepository;
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
