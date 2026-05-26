package jason.architecture.api.application.snapshot.port.out;

import jason.architecture.api.application.snapshot.model.AgentSnapshot;
import jason.architecture.api.application.snapshot.model.CycleAgentSnapshot;

import java.util.List;

public interface AgentSnapshotRepository {

    void saveSnapshot(AgentSnapshot agent, int cycleNumber);

    CycleAgentSnapshot findSnapshot(String agentName, Integer cycle);

    List<CycleAgentSnapshot> findLatestSnapshots();
}
