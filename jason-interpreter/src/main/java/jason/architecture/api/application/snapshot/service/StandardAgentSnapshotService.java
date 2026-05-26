package jason.architecture.api.application.snapshot.service;

import jason.architecture.api.application.snapshot.port.out.AgentSnapshotRepository;
import jason.architecture.api.application.snapshot.model.CycleAgentSnapshot;
import jason.architecture.api.application.snapshot.port.in.AgentSnapshotService;
import jason.architecture.api.application.shared.exception.AgentDoesNotExistException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class StandardAgentSnapshotService implements AgentSnapshotService {

    private final AgentSnapshotRepository agentSnapshotRepository;

    @Override
    public CycleAgentSnapshot findByNameAndCycle(String name, Integer cycle) {
        CycleAgentSnapshot state = this.agentSnapshotRepository.findSnapshot(name, cycle);
        if (state == null) {
            throw new AgentDoesNotExistException();
        }
        return state;
    }

    @Override
    public CycleAgentSnapshot findByName(String name) {
        CycleAgentSnapshot state = this.agentSnapshotRepository.findSnapshot(name, null);
        if (state == null) {
            throw new AgentDoesNotExistException();
        }
        return state;
    }

    @Override
    public List<CycleAgentSnapshot> findAll() {
        return this.agentSnapshotRepository.findLatestSnapshots();
    }

}
