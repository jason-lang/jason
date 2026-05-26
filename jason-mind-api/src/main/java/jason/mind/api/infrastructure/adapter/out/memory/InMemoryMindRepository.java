package jason.mind.api.infrastructure.adapter.out.memory;

import jason.mind.api.application.log.model.AgentLog;
import jason.mind.api.application.snapshot.model.AgentSnapshot;
import jason.mind.api.application.snapshot.model.CycleAgentSnapshot;
import jason.mind.api.application.snapshot.model.CycleInfo;
import jason.mind.api.application.snapshot.model.MessageSnapshot;
import jason.mind.api.application.log.port.out.AgentLogRepository;
import jason.mind.api.application.snapshot.port.out.AgentMessageRepository;
import jason.mind.api.application.snapshot.port.out.AgentSnapshotRepository;

import java.util.*;

public class InMemoryMindRepository implements AgentSnapshotRepository, AgentLogRepository, AgentMessageRepository {

    private static final int STATE_QUEUE_MAX_SIZE = 10000;

    private static InMemoryMindRepository instance;

    private final Map<String, TreeMap<Integer, CycleAgentSnapshot>> agentStates;

    private final Map<String, List<AgentLog>> agentLogs;

    private final Map<String, List<MessageSnapshot>> sendedMessages;

    public InMemoryMindRepository() {
        this.agentStates = new HashMap<>();
        this.agentLogs = new HashMap<>();
        this.sendedMessages = new HashMap<>();
    }

    public synchronized static InMemoryMindRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryMindRepository();
        }
        return instance;
    }

    public synchronized void saveLog(String agentName, AgentLog agentLog) {
        List<AgentLog> logs = this.agentLogs.get(agentName);
        if (logs == null) {
            logs = new ArrayList<>();
        }
        logs.add(agentLog);
        this.agentLogs.put(agentName, logs);
    }

    public synchronized AgentLog findLog(String agentName, int cycle) {
        List<AgentLog> logs = this.agentLogs.get(agentName);
        for (AgentLog log : logs) {
            if (log.getCycle() == cycle) {
                return log;
            }
        }
        return null;
    }

    public synchronized List<AgentLog> findLogs(String agentName) {
        return this.agentLogs.get(agentName);
    }

    public synchronized List<AgentLog> findAllLogs() {
        List<AgentLog> logs = new ArrayList<>();
        for (Map.Entry<String, List<AgentLog>> agentNameByLogs : this.agentLogs.entrySet()) {
            logs.addAll(agentNameByLogs.getValue());
        }
        logs.sort(Comparator.comparing(AgentLog::getTime));
        return logs;
    }

    public synchronized List<MessageSnapshot> findInMessagesUntil(String agentName, Date time) {
        TreeMap<Integer, CycleAgentSnapshot> agentCycles = this.agentStates.get(agentName);

        if (agentCycles == null) {
            return null;
        }

        if (time == null) {
            return new ArrayList<>();
        }

        List<MessageSnapshot> messages = new ArrayList<>();
        for (Map.Entry<Integer, CycleAgentSnapshot> cycleNumberAndAgentState : agentCycles.entrySet()) {
            if (cycleNumberAndAgentState.getValue().getTime().getTime() > time.getTime()) {
                continue;
            }

            messages.addAll(cycleNumberAndAgentState.getValue().getMessageBox());
        }

        return messages;
    }

    public synchronized List<MessageSnapshot> findOutMessagesUntil(String agentName, Date time) {
        return this.sendedMessages.computeIfAbsent(agentName, k -> new ArrayList<>()).stream().filter(
                m -> m.getTime().getTime() <= time.getTime()).toList();
    }

    public synchronized List<MessageSnapshot> findAllMessages() {
        List<MessageSnapshot> messages = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Integer, CycleAgentSnapshot>> agentStateHistoryByAgentName :
                this.agentStates.entrySet()) {
            for (Map.Entry<Integer, CycleAgentSnapshot> agentStateByCycle : agentStateHistoryByAgentName.getValue()
                    .entrySet()) {
                CycleAgentSnapshot agent = agentStateByCycle.getValue();
                messages.addAll(agent.getMessageBox());
            }
        }
        messages.sort(Comparator.comparing(MessageSnapshot::getId));
        return messages;
    }

    public synchronized void saveSnapshot(AgentSnapshot agent, int cycleNumber) {
        CycleAgentSnapshot cycleAgent = new CycleAgentSnapshot(agent, new CycleInfo(cycleNumber));

        TreeMap<Integer, CycleAgentSnapshot> history = this.agentStates.computeIfAbsent(agent.getName(),
                k -> new TreeMap<>());

        if (history.size() == STATE_QUEUE_MAX_SIZE) {
            history.pollFirstEntry();
        }

        for (MessageSnapshot message : agent.getMessageBox()) {
            this.sendedMessages.computeIfAbsent(message.getSender(), k -> new ArrayList<>()).add(message);
        }

        history.put(cycleAgent.getCycleInfo().getCurrentCycleNumber(), cycleAgent);
    }

    public synchronized CycleAgentSnapshot findSnapshot(String agentName, Integer cycle) {
        TreeMap<Integer, CycleAgentSnapshot> history = this.agentStates.get(agentName);
        if (history == null) {
            return null;
        }

        int newerCycleNumber = history.lastEntry().getValue().getCycleInfo().getCurrentCycleNumber();
        if (cycle == null) {
            cycle = newerCycleNumber;
        }

        CycleAgentSnapshot agentSnapshot = history.get(cycle);
        if (agentSnapshot == null) {
            return null;
        }

        int olderCycleNumber = history.firstEntry().getValue().getCycleInfo().getCurrentCycleNumber();
        agentSnapshot.getCycleInfo().setNewerCycleNumber(newerCycleNumber);
        agentSnapshot.getCycleInfo().setOlderCycleNumber(olderCycleNumber);

        return agentSnapshot;
    }

    public synchronized List<CycleAgentSnapshot> findLatestSnapshots() {
        List<CycleAgentSnapshot> returnAgentSnapshots = new ArrayList<>();
        for (String agentName : this.agentStates.keySet()) {
            CycleAgentSnapshot agentState = this.findSnapshot(agentName, null);
            returnAgentSnapshots.add(agentState);
        }
        return returnAgentSnapshots;
    }
}
