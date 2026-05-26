package jason.mind.api.application.runtime.service;

import jason.mind.api.application.runtime.model.*;
import jason.mind.api.application.runtime.port.in.AgentRuntimeService;
import jason.mind.api.application.shared.exception.AgentDoesNotExistException;
import jason.mind.api.application.shared.exception.JasonParserException;
import jason.mind.api.application.shared.port.out.JasonRuntimeGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StandardAgentRuntimeService implements AgentRuntimeService {

    private final JasonRuntimeGateway runtimeGateway;

    private void ensureAgentExists(String agentName) {
        if (!this.runtimeGateway.agentExists(agentName)) {
            throw new AgentDoesNotExistException();
        }
    }

    @Override
    public CommandOut executeCommand(String name, CommandIn command) throws JasonParserException {
        this.ensureAgentExists(name);
        return this.runtimeGateway.executeCommand(name, command);
    }

    @Override
    public void create(CreateAgentIn createAgentIn) throws JasonParserException {
        this.runtimeGateway.createAgent(createAgentIn);
    }

    @Override
    public void kill(String name) {
        this.ensureAgentExists(name);
        this.runtimeGateway.killAgent(name);
    }

    @Override
    public void addPlan(String name, CreatePlanIn createPlanIn) throws JasonParserException {
        this.ensureAgentExists(name);
        this.runtimeGateway.addPlan(name, createPlanIn);
    }

    @Override
    public void sendMessage(String name, MessageIn messageIn) throws JasonParserException {
        this.ensureAgentExists(name);
        this.runtimeGateway.sendMessage(name, messageIn);
    }
}
