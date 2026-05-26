package jason.architecture.api.application.runtime.port.in;

import jason.architecture.api.application.runtime.model.*;
import jason.architecture.api.application.shared.exception.JasonParserException;

public interface AgentRuntimeService {

    CommandOut executeCommand(String name, CommandIn commandIn) throws JasonParserException;

    void create(CreateAgentIn createAgentIn) throws JasonParserException;

    void kill(String name);

    void addPlan(String name, CreatePlanIn createPlanIn) throws JasonParserException;

    void sendMessage(String name, MessageIn messageIn) throws JasonParserException;

}
