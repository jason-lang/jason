package jason.mind.api.application.snapshot.port.out;

import jason.mind.api.application.snapshot.model.AgentSnapshot;
import jason.mind.api.application.snapshot.model.CycleAgentSnapshot;

import java.util.List;

public interface AgentSnapshotRepository {

    void saveSnapshot(AgentSnapshot agent, int cycleNumber);

    CycleAgentSnapshot findSnapshot(String agentName, Integer cycle);

    List<CycleAgentSnapshot> findLatestSnapshots();
}
