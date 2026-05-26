package jason.architecture.api.application.log.port.out;

import jason.architecture.api.application.log.model.AgentLog;

import java.util.List;

public interface AgentLogRepository {

    void saveLog(String agentName, AgentLog agentLog);

    AgentLog findLog(String agentName, int cycle);

    List<AgentLog> findLogs(String agentName);

    List<AgentLog> findAllLogs();
}
