package jason.mind.api.infrastructure.adapter.in.jason;

import jason.mind.api.application.log.model.AgentLog;
import jason.mind.api.application.log.port.in.AgentLogService;
import jason.mind.api.application.log.port.out.AgentLogRepository;
import jason.asSemantics.Agent;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AgentLogListener extends Handler {

    private final Agent agent;

    private final AgentLogService agentLogService;

    public AgentLogListener(Agent agent, AgentLogService agentLogService) {
        this.agent = agent;
        this.agentLogService = agentLogService;
    }

    @Override
    public void publish(LogRecord record) {
        Date logTime = new Date(record.getInstant().toEpochMilli());
        String agentName = this.agent.getTS().getAgArch().getAgName();
        int cycleNumber = this.agent.getTS().getAgArch().getCycleNumber();
        this.agentLogService.saveLog(agentName, new AgentLog(agentName, record.getMessage(), logTime, cycleNumber));
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
