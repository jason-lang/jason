package jason.architecture.api.application.snapshot.port.in;

import jason.architecture.api.application.snapshot.model.CycleAgentSnapshot;

import java.util.List;

public interface AgentSnapshotService {

    CycleAgentSnapshot findByNameAndCycle(String name, Integer cycle);

    CycleAgentSnapshot findByName(String name);

    List<CycleAgentSnapshot> findAll();

}
