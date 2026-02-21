package jason.architecture.api.app.service.agent;

import jason.architecture.api.app.exception.AgentDoesNotExistException;
import jason.architecture.api.app.exception.JasonParserException;
import jason.architecture.api.app.handler.AgentWrapperHandler;
import jason.architecture.api.app.handler.MessageWrapperHandler;
import jason.architecture.api.app.handler.PlanWrapperHandler;
import jason.architecture.api.app.state.AgentLog;
import jason.architecture.api.app.model.Message;
import jason.architecture.api.app.model.command.CommandIn;
import jason.architecture.api.app.model.command.CommandOut;
import jason.architecture.api.app.model.content.AgentContent;
import jason.architecture.api.app.model.content.PlanContent;
import jason.architecture.api.app.model.state.AgentWrapper;
import jason.architecture.api.app.model.state.CycleAgentWrapper;
import jason.architecture.api.app.model.state.MessageWrapper;
import jason.architecture.api.app.model.state.plan.PlanDeedWrapper;
import jason.architecture.api.app.model.state.plan.PlanWrapper;
import jason.architecture.api.app.state.AgentStateManager;
import jason.architecture.api.infra.JasonUtils;
import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.TokenMgrError;
import jason.infra.local.LocalAgArch;
import jason.pl.PlanLibraryListener;
import jason.runtime.RuntimeServicesFactory;
import jason.runtime.Settings;

import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StandardAgentService implements AgentService {

    private static final Executor EXEC = Executors.newFixedThreadPool(4);

    private static final Trigger RUN_COMMAND_TRIGGER;

    private static long mindApiPlanCounter = 1;

    static {
        LiteralImpl runCommandTrigger = new LiteralImpl("run_command");
        LiteralImpl sourceTrigger = new LiteralImpl("source");
        runCommandTrigger.addAnnot(sourceTrigger);

        RUN_COMMAND_TRIGGER = new Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, runCommandTrigger);
    }

    private Pred mountPlanLabel() {
        Pred label = new Pred("plan__mindapi__" + mindApiPlanCounter);
        Structure source = new Structure("source");
        source.addTerm(new Atom("mindapi"));
        label.addAnnot(source);
        return label;
    }

    @Override
    public List<MessageWrapper> findInMessagesUntilTime(String name, Date time) {
        return AgentStateManager.getInstance().getInMessagesUntilTime(name, time);
    }

    @Override
    public List<MessageWrapper> findOutMessagesUntilTime(String name, Date time) {
        return AgentStateManager.getInstance().getOutMessagesUntilTime(name, time);
    }

    @Override
    public CycleAgentWrapper findByNameAndCycle(String name, Integer cycle) {
        CycleAgentWrapper state = AgentStateManager.getInstance().getState(name, cycle);
        if (state == null) {
            throw new AgentDoesNotExistException();
        }
        return state;
    }

    @Override
    public CycleAgentWrapper findByName(String name) {
        CycleAgentWrapper state = AgentStateManager.getInstance().getState(name, null);
        if (state == null) {
            throw new AgentDoesNotExistException();
        }
        return state;
    }

    @Override
    public List<CycleAgentWrapper> findAll() {
        return AgentStateManager.getInstance().getAllStates();
    }

    @Override
    public CommandOut executeCommand(String name, CommandIn command) throws JasonParserException {
        Agent agent = JasonUtils.getAgentFromSMA(name);
        if (agent == null) {
            throw new AgentDoesNotExistException();
        }

        PlanBody commandPlanBody;
        try {
            commandPlanBody = ASSyntax.parsePlanBody(command.getCommand());
        } catch (TokenMgrError | ParseException e) {
            throw new JasonParserException(e.getMessage());
        }

        Plan plan = new Plan(null, RUN_COMMAND_TRIGGER, null, commandPlanBody);
        Unifier unifier = new Unifier();

        Intention intention = new Intention();
        IntendedMeans intendedMeans = new IntendedMeans(new Option(plan, unifier), RUN_COMMAND_TRIGGER);
        intention.push(intendedMeans);

        Lock lock = new ReentrantLock();
        Condition goalFinished = lock.newCondition();
        EXEC.execute(() -> {
            CircumstanceListener cl = new CircumstanceListener() {
                public void intentionAdded(Intention ci) {
                    if (intention.equals(ci)) {
                        try {
                            lock.lock();
                            goalFinished.signalAll();
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            };
            TransitionSystem ts = agent.getTS();
            try {
                lock.lock();
                ts.getC().addEventListener(cl);
                ts.getC().addRunningIntention(intention);
                ts.getAgArch().wake();
                goalFinished.await();
                ts.getC().removeEventListener(cl);
            } catch (InterruptedException ignored) {
            } finally {
                lock.unlock();
            }
        });
        try {
            lock.lock();
            goalFinished.await();
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }

        PlanDeedWrapper deed = new PlanWrapperHandler(agent).extractPlanBody(commandPlanBody, unifier);

        return new CommandOut(deed, agent.getTS().getAgArch().getCycleNumber());
    }

    @Override
    public AgentWrapper create(AgentContent agentContent) throws JasonParserException {
        if (agentContent.getName() == null) {
            return null;
        }

        List<Plan> plans = new ArrayList<>();
        for (PlanContent planContent : agentContent.getPlans()) {
            Plan plan;
            try {
                plan = ASSyntax.parsePlan(planContent.getContent());
                plan.setLabel(this.mountPlanLabel());
                plans.add(plan);
            } catch (ParseException e) {
                throw new JasonParserException(e.getMessage());
            }
            mindApiPlanCounter++;
        }

        List<Literal> goals = new ArrayList<>();
        for (String goal : agentContent.getInitialGoals()) {
            try {
                Literal literalGoal = ASSyntax.parseLiteral(goal);
                goals.add(literalGoal);
            } catch (ParseException | TokenMgrError e) {
                throw new JasonParserException(e.getMessage());
            }
        }

        List<Literal> beliefs = new ArrayList<>();
        for (String belief : agentContent.getInitialBeliefs()) {
            try {
                Literal literalBelief = ASSyntax.parseLiteral(belief);
                beliefs.add(literalBelief);
            } catch (ParseException | TokenMgrError e) {
                throw new JasonParserException(e.getMessage());
            }
        }

        Settings settings = new Settings();
        settings.addOption(Settings.MIND_INSPECTOR, "");
        String agentName;
        try {
            agentName = RuntimeServicesFactory.get().createAgent(agentContent.getName(), null, null, null, null,
                    settings, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Agent agentFromSMA = JasonUtils.getAgentFromSMA(agentName);

        for (Literal belief : beliefs) {
            agentFromSMA.addInitialBel(belief);
        }

        for (Literal goal : goals) {
            agentFromSMA.addInitialGoal(goal);
        }

        try {
            agentFromSMA.getPL().addAll(plans);
            agentFromSMA.addInitialGoalsInTS();
            agentFromSMA.addInitialBelsInBB();
        } catch (JasonException e) {
            throw new RuntimeException(e);
        }

        try {
            RuntimeServicesFactory.get().startAgent(agentName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        return new AgentWrapperHandler().extract(agentFromSMA);
    }

    @Override
    public CycleAgentWrapper kill(String name) {
        Agent agent = JasonUtils.getAgentFromSMA(name);
        if (agent == null) {
            throw new AgentDoesNotExistException();
        }
        CycleAgentWrapper lastState = this.findByName(name);
        try {
            RuntimeServicesFactory.get().killAgent(name, null, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return lastState;
    }

    @Override
    public PlanWrapper addPlan(String name, PlanContent planContent) throws JasonParserException {
        Agent agent = JasonUtils.getAgentFromSMA(name);
        if (agent == null) {
            throw new AgentDoesNotExistException();
        }

        Plan plan;
        try {
            plan = ASSyntax.parsePlan(planContent.getContent());
            plan.setLabel(this.mountPlanLabel());
        } catch (ParseException e) {
            throw new JasonParserException(e.getMessage());
        }

        Lock lock = new ReentrantLock();
        Condition goalFinished = lock.newCondition();

        EXEC.execute(() -> {
            try {
                agent.getPL().addListener(new PlanLibraryListener() {

                    @Override
                    public void planAdded(Plan addedPlan) {
                        if (addedPlan.equals(plan)) {
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }

                    @Override
                    public void planRemoved(Plan plan) {

                    }
                });

                lock.lock();
                agent.getPL().add(plan);
                goalFinished.await();
            } catch (InterruptedException ignored) {
            } catch (JasonException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        });

        try {
            lock.lock();
            goalFinished.await();
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }

        mindApiPlanCounter++;

        return new PlanWrapperHandler(agent).extractPlan(plan, null, null);
    }

    @Override
    public MessageWrapper sendMessage(String name, Message message)
    throws JasonParserException, InvalidParameterException {
        Agent agent = JasonUtils.getAgentFromSMA(name);
        if (agent == null) {
            throw new AgentDoesNotExistException();
        }

        if (message.getPerformative() == null || message.getContent() == null || message.getContent().isEmpty()
                || message.getPerformative().isEmpty()) {
            throw new InvalidParameterException();
        }

        boolean isValidPerformative = false;
        for (String knownPerformative : jason.asSemantics.Message.knownPerformatives) {
            if (knownPerformative.equals(message.getPerformative())) {
                isValidPerformative = true;
                break;
            }
        }

        if (!isValidPerformative) {
            throw new InvalidParameterException(
                    "Valid performatives : " + Arrays.toString(jason.asSemantics.Message.knownPerformatives));
        }

        LocalAgArch localAgArch = JasonUtils.getLocalAgArch(agent);

        Term contentTerm;
        try {
            contentTerm = ASSyntax.parseTerm(message.getContent());
        } catch (ParseException e) {
            throw new JasonParserException(e.getMessage());
        }

        if (localAgArch == null) {
            return null;
        }
        jason.asSemantics.Message parsedMessage = new jason.asSemantics.Message(message.getPerformative(), "mindapi",
                name, contentTerm);
        localAgArch.receiveMsg(parsedMessage);

        return new MessageWrapperHandler(agent, new Date()).extract(parsedMessage);
    }

    @Override
    public AgentLog getLog(String agentName, int cycle) {
        if (JasonUtils.getAgentFromSMA(agentName) == null) {
            throw new AgentDoesNotExistException();
        }
        return AgentStateManager.getInstance().getLog(agentName, cycle);
    }

    @Override
    public List<AgentLog> getLogs(String agentName) {
        if (JasonUtils.getAgentFromSMA(agentName) == null) {
            throw new AgentDoesNotExistException();
        }
        return AgentStateManager.getInstance().getLogs(agentName);
    }
}
