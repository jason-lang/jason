package jason.architecture.api.web.api;

import jason.architecture.api.app.exception.AgentDoesNotExistException;
import jason.architecture.api.app.exception.JasonParserException;
import jason.architecture.api.app.model.Message;
import jason.architecture.api.app.model.command.CommandIn;
import jason.architecture.api.app.model.content.AgentContent;
import jason.architecture.api.app.model.content.PlanContent;
import jason.architecture.api.app.model.state.CycleAgentWrapper;
import jason.architecture.api.app.model.state.MessageWrapper;
import jason.architecture.api.app.service.agent.AgentService;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Request;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;
import net.peelweb.enums.HttpMethod;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;

import static net.peelweb.context.endpoint.Responses.*;

@Controller("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @Mapping("/{name}")
    public Response get(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Integer cycle = request.getParameterAsInteger("cycle");
        try {
            CycleAgentWrapper stateByCycle = this.agentService.findByNameAndCycle(name, cycle);
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
        return ok(((CycleAgentWrapper) response.getBody()).getBeliefs());
    }

    @Mapping("/{name}/plans")
    public Response getPlans(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentWrapper) response.getBody()).getAllPlans());
    }

    @Mapping("/{name}/plans/running")
    public Response getRunningPlans(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentWrapper) response.getBody()).getRunningPlans());
    }

    @Mapping(value = "/{name}/plans", method = HttpMethod.POST)
    public Response addPlan(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        PlanContent plan = request.getBodyAs(PlanContent.class);
        try {
            return Responses.created(this.agentService.addPlan(name, plan));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        } catch (JasonParserException e) {
            return badRequest();
        }
    }

    @Mapping("/{name}/intentions")
    public Response getIntentions(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentWrapper) response.getBody()).getIntentions());
    }

    @Mapping("/{name}/rules")
    public Response getRules(Request request) {
        Response response = this.get(request);
        return ok(((CycleAgentWrapper) response.getBody()).getRules());
    }

    @Mapping("/{name}/messages/in")
    public Response getMessagesIn(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Long time = request.getParameterAs("time", Long::parseLong);
        try {
            List<MessageWrapper> messages = this.agentService.findInMessagesUntilTime(name, new Date(time));
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
            List<MessageWrapper> messages = this.agentService.findOutMessagesUntilTime(name, new Date(time));
            if (messages == null) {
                return notFound();
            }
            return ok(messages);
        } catch (AgentDoesNotExistException e) {
            return badRequest();
        }
    }

    @Mapping(value = "/{name}/command", method = HttpMethod.POST)
    public Response executeCommand(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        CommandIn commandIn = request.getBodyAs(CommandIn.class);
        if (commandIn == null || commandIn.getCommand() == null || commandIn.getCommand().isEmpty()) {
            return badRequest();
        }

        try {
            return ok(this.agentService.executeCommand(name, commandIn));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        } catch (JasonParserException e) {
            return badRequest();
        }
    }

    @Mapping(method = HttpMethod.POST)
    public Response create(Request request) {
        AgentContent agentContentReq = request.getBodyAs(AgentContent.class);
        if (agentContentReq.getName() == null) {
            return badRequest();
        }

        try {
            return ok(this.agentService.create(agentContentReq));
        } catch (JasonParserException e) {
            return badRequest();
        }
    }

    @Mapping(value = "/{name}", method = HttpMethod.DELETE)
    public Response delete(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        try {
            return ok(this.agentService.kill(name));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        }
    }

    @Mapping(value = "/{name}/inbox", method = HttpMethod.POST)
    public Response sendMessage(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        Message message = request.getBodyAs(Message.class);
        try {
            return Responses.created(this.agentService.sendMessage(name, message));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        } catch (InvalidParameterException | JasonParserException e) {
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
            return ok(cycle == null ? this.agentService.getLogs(name) : this.agentService.getLog(name, cycle));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        }
    }

    @Mapping
    public Response getAll() {
        List<CycleAgentWrapper> agents = this.agentService.findAll();
        return ok(agents);
    }
}
