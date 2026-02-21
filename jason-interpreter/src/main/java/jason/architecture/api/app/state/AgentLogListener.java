package jason.architecture.api.app.state;

import jason.asSemantics.Agent;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

@RequiredArgsConstructor
public class AgentLogListener extends Handler {

    private final Agent agent;

    @Override
    public void publish(LogRecord record) {
        Date logTime = new Date(record.getInstant().toEpochMilli());
        String agentName = this.agent.getTS().getAgArch().getAgName();
        int cycleNumber = this.agent.getTS().getAgArch().getCycleNumber();
        AgentStateManager.getInstance().addLog(agentName, new AgentLog(agentName, record.getMessage(), logTime, cycleNumber));
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
