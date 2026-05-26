package jason.architecture.api.application.log.port.in;

import jason.architecture.api.application.log.model.AgentLog;

import java.util.List;

public interface AgentLogService {

    void saveLog(String agentName, AgentLog agentLog);

    AgentLog findLog(String agentName, int cycle);

    List<AgentLog> findLogs(String agentName);

    List<AgentLog> findAllLogs();

}
