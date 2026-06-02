package jason.mind.api.infrastructure.adapter.in.web.api;

import jason.mind.api.application.log.port.in.AgentLogService;
import jason.mind.api.application.shared.exception.AgentDoesNotExistException;
import jason.mind.api.application.snapshot.model.CycleAgentSnapshot;
import jason.mind.api.application.snapshot.model.MessageSnapshot;
import jason.mind.api.application.snapshot.port.in.AgentSnapshotService;
import jason.mind.api.application.snapshot.port.in.MessageSnapshotService;
import lombok.RequiredArgsConstructor;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Request;
import net.peelweb.context.endpoint.Response;

import java.util.Date;
import java.util.List;

import static net.peelweb.context.endpoint.Responses.*;

@Controller("/api/agents")
@RequiredArgsConstructor
public class AgentStateController {

    private final AgentSnapshotService agentSnapshotService;

    private final MessageSnapshotService messageSnapshotService;

    private final AgentLogService agentLogService;

    @Mapping("/{name}")
    public Response get(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Integer cycle = request.getParameterAsInteger("cycle");
        try {
            CycleAgentSnapshot stateByCycle = this.agentSnapshotService.findByNameAndCycle(name, cycle);
            if (stateByCycle == null) {
                return notFound();
            }
            return ok(stateByCycle);
        } catch (AgentDoesNotExistException e) {
            return badRequest();
        }
    }

    @Mapping("/{name}/beliefs")
    public Response getBeliefs(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentSnapshot) response.getBody()).getBeliefs());
    }

    @Mapping("/{name}/plans")
    public Response getPlans(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentSnapshot) response.getBody()).getAllPlans());
    }

    @Mapping("/{name}/plans/running")
    public Response getRunningPlans(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentSnapshot) response.getBody()).getRunningPlans());
    }

    @Mapping("/{name}/intentions")
    public Response getIntentions(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentSnapshot) response.getBody()).getIntentions());
    }

    @Mapping("/{name}/rules")
    public Response getRules(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentSnapshot) response.getBody()).getRules());
    }

    @Mapping("/{name}/messages/in")
    public Response getMessagesIn(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Long time = request.getParameterAs("time", Long::parseLong);
        try {
            List<MessageSnapshot> messages = this.messageSnapshotService.findInMessagesUntilTime(name, new Date(time));
            if (messages == null) {
                return notFound();
            }
            return ok(messages);
        } catch (AgentDoesNotExistException e) {
            return badRequest();
        }
    }

    @Mapping("/{name}/messages/out")
    public Response getMessagesOut(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Long time = request.getParameterAs("time", Long::parseLong);
        try {
            List<MessageSnapshot> messages = this.messageSnapshotService.findOutMessagesUntilTime(name, new Date(time));
            if (messages == null) {
                return notFound();
            }
            return ok(messages);
        } catch (AgentDoesNotExistException e) {
            return badRequest();
        }
    }

    @Mapping("/{name}/logs")
    public Response getLogs(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Integer cycle = request.getParameterAsInteger("cycle");
        try {
            return ok(cycle == null ? this.agentLogService.findLogs(name) : this.agentLogService.findLog(name, cycle));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        }
    }

    @Mapping
    public Response getAll() {
        List<CycleAgentSnapshot> agents = this.agentSnapshotService.findAll();
        return ok(agents);
    }
}
