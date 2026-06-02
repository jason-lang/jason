package jason.mind.api.application.log.service;

import jason.mind.api.application.log.model.AgentLog;
import jason.mind.api.application.log.port.in.AgentLogService;
import jason.mind.api.application.log.port.out.AgentLogRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class StandardAgentLogService implements AgentLogService {

    private final AgentLogRepository logRepository;

    @Override
    public void saveLog(String agentName, AgentLog agentLog) {
        this.logRepository.saveLog(agentName, agentLog);
    }

    @Override
    public AgentLog findLog(String agentName, int cycle) {
        return this.logRepository.findLog(agentName, cycle);
    }

    @Override
    public List<AgentLog> findLogs(String agentName) {
        return this.logRepository.findLogs(agentName);
    }

    @Override
    public List<AgentLog> findAllLogs() {
        return this.logRepository.findAllLogs();
    }
}
