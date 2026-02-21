package jason.architecture.api.app.service.mas;

import jason.architecture.api.app.model.Mas;
import jason.architecture.api.app.state.AgentLog;
import jason.architecture.api.app.state.AgentStateManager;
import jason.architecture.api.infra.JasonUtils;

import java.util.List;

public class StandardMasService implements MasService {

    @Override
    public List<AgentLog> getLogs() {
        return AgentStateManager.getInstance().getAllLogs();
    }

    @Override
    public Mas getMas() {
        return new Mas(JasonUtils.getMasName());
    }

}
