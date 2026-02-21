package jason.architecture.api.app.state;

import jason.architecture.api.app.model.state.AgentWrapper;
import jason.architecture.api.app.model.state.CycleAgentWrapper;
import jason.architecture.api.app.model.state.CycleInfo;
import jason.architecture.api.app.model.state.MessageWrapper;

import java.util.*;

public class AgentStateManager {

    private static final int STATE_QUEUE_MAX_SIZE = 10000;

    private static AgentStateManager instance;

    private final Map<String, TreeMap<Integer, CycleAgentWrapper>> agentStates;

    private final Map<String, List<AgentLog>> agentLogs;

    private final Map<String, List<MessageWrapper>> sendedMessages;

    public AgentStateManager() {
        this.agentStates = new HashMap<>();
        this.agentLogs = new HashMap<>();
        this.sendedMessages = new HashMap<>();
    }

    public synchronized static AgentStateManager getInstance() {
        if (instance == null) {
            instance = new AgentStateManager();
        }
        return instance;
    }

    public synchronized void addLog(String agentName, AgentLog agentLog) {
        List<AgentLog> logs = this.agentLogs.get(agentName);
        if (logs == null) {
            logs = new ArrayList<>();
        }
        logs.add(agentLog);
        this.agentLogs.put(agentName, logs);
    }

    public synchronized AgentLog getLog(String agentName, int cycle) {
        List<AgentLog> logs = this.agentLogs.get(agentName);
        for (AgentLog log : logs) {
            if (log.getCycle() == cycle) {
                return log;
            }
        }
        return null;
    }

    public synchronized List<AgentLog> getLogs(String agentName) {
        return this.agentLogs.get(agentName);
    }

    public synchronized List<MessageWrapper> getOutMessagesUntilTime(String agentName, Date time) {
        return this.sendedMessages.computeIfAbsent(agentName, k -> new ArrayList<>()).stream().filter(
                m -> m.getTime().getTime() <= time.getTime()).toList();
    }

    public synchronized List<MessageWrapper> getInMessagesUntilTime(String agentName, Date time) {
        TreeMap<Integer, CycleAgentWrapper> agentCycles = this.agentStates.get(agentName);

        if (agentCycles == null) {
            return null;
        }

        if (time == null) {
            return new ArrayList<>();
        }

        List<MessageWrapper> messages = new ArrayList<>();
        for (Map.Entry<Integer, CycleAgentWrapper> cycleNumberAndAgentState : agentCycles.entrySet()) {
            if (cycleNumberAndAgentState.getValue().getTime().getTime() > time.getTime()) {
                continue;
            }

            messages.addAll(cycleNumberAndAgentState.getValue().getMessageBox());
        }

        return messages;
    }

    public synchronized CycleAgentWrapper getState(String agentName, Integer cycle) {
        TreeMap<Integer, CycleAgentWrapper> history = this.agentStates.get(agentName);
        if (history == null) {
            return null;
        }

        int newerCycleNumber = history.lastEntry().getValue().getCycleInfo().getCurrentCycleNumber();
        if (cycle == null) {
            cycle = newerCycleNumber;
        }

        CycleAgentWrapper agentWrapper = history.get(cycle);
        if (agentWrapper == null) {
            return null;
        }

        int olderCycleNumber = history.firstEntry().getValue().getCycleInfo().getCurrentCycleNumber();
        agentWrapper.getCycleInfo().setNewerCycleNumber(newerCycleNumber);
        agentWrapper.getCycleInfo().setOlderCycleNumber(olderCycleNumber);

        return agentWrapper;
    }

    public synchronized void addState(AgentWrapper agent, int cycleNumber) {
        CycleAgentWrapper cycleAgent = new CycleAgentWrapper(agent, new CycleInfo(cycleNumber));

        TreeMap<Integer, CycleAgentWrapper> history = this.agentStates.computeIfAbsent(agent.getName(),
                k -> new TreeMap<>());

        if (history.size() == STATE_QUEUE_MAX_SIZE) {
            history.pollFirstEntry();
        }

        for (MessageWrapper message : agent.getMessageBox()) {
            this.sendedMessages.computeIfAbsent(message.getSender(), k -> new ArrayList<>()).add(message);
        }

        history.put(cycleAgent.getCycleInfo().getCurrentCycleNumber(), cycleAgent);
    }

    public synchronized List<MessageWrapper> getAllMessages() {
        List<MessageWrapper> messages = new ArrayList<>();
        for (Map.Entry<String, TreeMap<Integer, CycleAgentWrapper>> agentStateHistoryByAgentName :
                this.agentStates.entrySet()) {
            for (Map.Entry<Integer, CycleAgentWrapper> agentStateByCycle : agentStateHistoryByAgentName.getValue()
                    .entrySet()) {
                CycleAgentWrapper agent = agentStateByCycle.getValue();
                messages.addAll(agent.getMessageBox());
            }
        }
        messages.sort(Comparator.comparing(MessageWrapper::getId));
        return messages;
    }

    public synchronized List<AgentLog> getAllLogs() {
        List<AgentLog> logs = new ArrayList<>();
        for (Map.Entry<String, List<AgentLog>> agentNameByLogs : this.agentLogs.entrySet()) {
            logs.addAll(agentNameByLogs.getValue());
        }
        logs.sort(Comparator.comparing(AgentLog::getTime));
        return logs;
    }

    public synchronized List<CycleAgentWrapper> getAllStates() {
        List<CycleAgentWrapper> returnAgentWrappers = new ArrayList<>();
        for (String agentName : this.agentStates.keySet()) {
            CycleAgentWrapper agentState = this.getState(agentName, null);
            returnAgentWrappers.add(agentState);
        }
        return returnAgentWrappers;
    }
}
