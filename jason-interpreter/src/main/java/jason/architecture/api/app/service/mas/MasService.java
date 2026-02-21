package jason.architecture.api.app.service.mas;

import jason.architecture.api.app.model.Mas;
import jason.architecture.api.app.state.AgentLog;

import java.util.List;

public interface MasService {

    List<AgentLog> getLogs();

    Mas getMas();

}
