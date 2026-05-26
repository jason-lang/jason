package jason.architecture.api.infrastructure.adapter.in.web.api;

import jason.architecture.api.application.runtime.model.CommandIn;
import jason.architecture.api.application.runtime.model.CreateAgentIn;
import jason.architecture.api.application.runtime.model.CreatePlanIn;
import jason.architecture.api.application.runtime.model.MessageIn;
import jason.architecture.api.application.runtime.port.in.AgentRuntimeService;
import jason.architecture.api.application.shared.exception.AgentDoesNotExistException;
import jason.architecture.api.application.shared.exception.JasonParserException;
import lombok.RequiredArgsConstructor;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Request;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;
import net.peelweb.enums.HttpMethod;
import net.peelweb.enums.HttpResponseCode;

import java.security.InvalidParameterException;

import static net.peelweb.context.endpoint.Responses.*;

@Controller("/api/runtime/agents")
@RequiredArgsConstructor
public class AgentRuntimeController {

    private final AgentRuntimeService agentRuntimeService;

    @Mapping(value = "/{name}/plans", method = HttpMethod.POST)
    public Response addPlan(Request request) {
        String name = request.getPathVariable("name");
        if (name == null) {
            return badRequest();
        }

        CreatePlanIn plan = request.getBodyAs(CreatePlanIn.class);
        try {
            this.agentRuntimeService.addPlan(name, plan);
            return Responses.mount(HttpResponseCode.OK);
        } catch (AgentDoesNotExistException e) {
            return notFound();
        } catch (JasonParserException e) {
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
            return ok(this.agentRuntimeService.executeCommand(name, commandIn));
        } catch (AgentDoesNotExistException e) {
            return notFound();
        } catch (JasonParserException e) {
            return badRequest();
        }
    }

    @Mapping(method = HttpMethod.POST)
    public Response create(Request request) {
        CreateAgentIn createAgentInReq = request.getBodyAs(CreateAgentIn.class);
        if (createAgentInReq.getName() == null) {
            return badRequest();
        }

        try {
            this.agentRuntimeService.create(createAgentInReq);
            return Responses.mount(HttpResponseCode.OK);
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
            this.agentRuntimeService.kill(name);
            return Responses.mount(HttpResponseCode.OK);
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

        MessageIn messageIn = request.getBodyAs(MessageIn.class);
        try {
            this.agentRuntimeService.sendMessage(name, messageIn);
            return Responses.mount(HttpResponseCode.OK);
        } catch (AgentDoesNotExistException e) {
            return notFound();
        } catch (InvalidParameterException | JasonParserException e) {
            return badRequest();
        }
    }

}
