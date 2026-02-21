package jason.architecture.api.app.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
@Getter
public class AgentLog {

    private final String agentName;

    private final String content;

    private final Date time;

    private final int cycle;

}
