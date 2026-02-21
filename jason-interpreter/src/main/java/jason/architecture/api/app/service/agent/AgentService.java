package jason.architecture.api.app.service.agent;

import jason.architecture.api.app.exception.JasonParserException;
import jason.architecture.api.app.state.AgentLog;
import jason.architecture.api.app.model.command.CommandIn;
import jason.architecture.api.app.model.content.AgentContent;
import jason.architecture.api.app.model.command.CommandOut;
import jason.architecture.api.app.model.Message;
import jason.architecture.api.app.model.content.PlanContent;
import jason.architecture.api.app.model.state.AgentWrapper;
import jason.architecture.api.app.model.state.CycleAgentWrapper;
import jason.architecture.api.app.model.state.MessageWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;

import java.util.Date;
import java.util.List;

public interface AgentService {

    List<MessageWrapper> findInMessagesUntilTime(String name, Date time);

    List<MessageWrapper> findOutMessagesUntilTime(String name, Date time);

    CycleAgentWrapper findByNameAndCycle(String name, Integer cycle);

    CycleAgentWrapper findByName(String name);

    List<CycleAgentWrapper> findAll();

    CommandOut executeCommand(String name, CommandIn commandIn) throws JasonParserException;

    AgentWrapper create(AgentContent agentContent) throws JasonParserException;

    CycleAgentWrapper kill(String name);

    PlanWrapper addPlan(String name, PlanContent planContent) throws JasonParserException;

    MessageWrapper sendMessage(String name, Message message) throws JasonParserException;

    AgentLog getLog(String agentName, int cycle);

    List<AgentLog> getLogs(String agentName);

}
