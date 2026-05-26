package jason.architecture.api.application.shared.port.out;

import jason.architecture.api.application.runtime.model.CreateAgentIn;
import jason.architecture.api.application.runtime.model.CommandIn;
import jason.architecture.api.application.runtime.model.CommandOut;
import jason.architecture.api.application.runtime.model.MessageIn;
import jason.architecture.api.application.runtime.model.CreatePlanIn;
import jason.architecture.api.application.shared.exception.JasonParserException;

import java.security.InvalidParameterException;

public interface JasonRuntimeGateway {

    boolean agentExists(String name);

    void createAgent(CreateAgentIn createAgentIn) throws JasonParserException;

    CommandOut executeCommand(String name, CommandIn command) throws JasonParserException;

    void addPlan(String name, CreatePlanIn createPlanIn) throws JasonParserException;

    void sendMessage(String name, MessageIn messageIn) throws JasonParserException, InvalidParameterException;

    void killAgent(String name);

    String getMasName();
}
