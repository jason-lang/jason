package jason.architecture.api.infrastructure.adapter.out.jason;

import jason.JasonException;
import jason.architecture.api.application.runtime.model.CreateAgentIn;
import jason.architecture.api.application.snapshot.model.plan.PlanDeedSnapshot;
import jason.architecture.api.application.runtime.model.CommandIn;
import jason.architecture.api.application.runtime.model.CommandOut;
import jason.architecture.api.application.runtime.model.MessageIn;
import jason.architecture.api.application.runtime.model.CreatePlanIn;
import jason.architecture.api.application.shared.exception.AgentDoesNotExistException;
import jason.architecture.api.application.shared.exception.JasonParserException;
import jason.architecture.api.application.shared.port.out.JasonRuntimeGateway;
import jason.architecture.api.infrastructure.mapper.PlanSnapshotMapper;
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
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultJasonRuntimeGateway implements JasonRuntimeGateway {

    private static final Executor EXEC = Executors.newFixedThreadPool(4);

    private static final Trigger RUN_COMMAND_TRIGGER;

    private static long mindApiPlanCounter = 1;

    static {
        LiteralImpl runCommandTrigger = new LiteralImpl("run_command");
        LiteralImpl sourceTrigger = new LiteralImpl("source");
        runCommandTrigger.addAnnot(sourceTrigger);

        RUN_COMMAND_TRIGGER = new Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, runCommandTrigger);
    }

    private Agent findAgent(String name) {
        return JasonUtils.getAgentFromSMA(name);
    }

    private String createRuntimeAgent(String name, Settings settings) throws Exception {
        return RuntimeServicesFactory.get().createAgent(name, null, null, null, null, settings, null);
    }

    private void startRuntimeAgent(String name) throws RemoteException {
        RuntimeServicesFactory.get().startAgent(name);
    }

    private boolean killRuntimeAgent(String name, String requestedBy, int deadline) throws RemoteException {
        return RuntimeServicesFactory.get().killAgent(name, requestedBy, deadline);
    }

    private Agent findExistingAgent(String name) {
        Agent agent = this.findAgent(name);
        if (agent == null) {
            throw new AgentDoesNotExistException();
        }
        return agent;
    }

    private Pred mountPlanLabel() {
        Pred label = new Pred("plan__mindapi__" + mindApiPlanCounter);
        Structure source = new Structure("source");
        source.addTerm(new Atom("mindapi"));
        label.addAnnot(source);
        return label;
    }

    @Override
    public boolean agentExists(String name) {
        return this.findAgent(name) != null;
    }

    @Override
    public void createAgent(CreateAgentIn createAgentIn) throws JasonParserException {
        if (createAgentIn.getName() == null) {
            throw new RuntimeException();
        }

        List<Plan> plans = new ArrayList<>();
        for (CreatePlanIn createPlanIn : createAgentIn.getPlans()) {
            Plan plan;
            try {
                plan = ASSyntax.parsePlan(createPlanIn.getContent());
                plan.setLabel(this.mountPlanLabel());
                plans.add(plan);
            } catch (ParseException e) {
                throw new JasonParserException(e.getMessage());
            }
            mindApiPlanCounter++;
        }

        List<Literal> goals = new ArrayList<>();
        for (String goal : createAgentIn.getInitialGoals()) {
            try {
                Literal literalGoal = ASSyntax.parseLiteral(goal);
                goals.add(literalGoal);
            } catch (ParseException | TokenMgrError e) {
                throw new JasonParserException(e.getMessage());
            }
        }

        List<Literal> beliefs = new ArrayList<>();
        for (String belief : createAgentIn.getInitialBeliefs()) {
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
            agentName = this.createRuntimeAgent(createAgentIn.getName(), settings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Agent agent = this.findAgent(agentName);

        for (Literal belief : beliefs) {
            agent.addInitialBel(belief);
        }

        for (Literal goal : goals) {
            agent.addInitialGoal(goal);
        }

        try {
            agent.getPL().addAll(plans);
            agent.addInitialGoalsInTS();
            agent.addInitialBelsInBB();
        } catch (JasonException e) {
            throw new RuntimeException(e);
        }

        try {
            this.startRuntimeAgent(agentName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommandOut executeCommand(String name, CommandIn command) throws JasonParserException {
        Agent agent = this.findExistingAgent(name);

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

        PlanDeedSnapshot deed = new PlanSnapshotMapper(agent).extractPlanBody(commandPlanBody, unifier);

        return new CommandOut(deed, agent.getTS().getAgArch().getCycleNumber());
    }

    @Override
    public void addPlan(String name, CreatePlanIn createPlanIn) throws JasonParserException {
        Agent agent = this.findExistingAgent(name);

        Plan plan;
        try {
            plan = ASSyntax.parsePlan(createPlanIn.getContent());
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
    }

    @Override
    public void sendMessage(String name, MessageIn messageIn)
    throws JasonParserException, InvalidParameterException {
        Agent agent = this.findExistingAgent(name);

        if (messageIn.getPerformative() == null || messageIn.getContent() == null || messageIn.getContent().isEmpty()
                || messageIn.getPerformative().isEmpty()) {
            throw new InvalidParameterException();
        }

        boolean isValidPerformative = false;
        for (String knownPerformative : jason.asSemantics.Message.knownPerformatives) {
            if (knownPerformative.equals(messageIn.getPerformative())) {
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
            contentTerm = ASSyntax.parseTerm(messageIn.getContent());
        } catch (ParseException e) {
            throw new JasonParserException(e.getMessage());
        }

        if (localAgArch == null) {
            throw new RuntimeException();
        }
        jason.asSemantics.Message parsedMessage = new jason.asSemantics.Message(messageIn.getPerformative(), "mindapi",
                name, contentTerm);
        localAgArch.receiveMsg(parsedMessage);
    }

    @Override
    public void killAgent(String name) {
        try {
            this.killRuntimeAgent(name, null, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getMasName() {
        return JasonUtils.getMasName();
    }
}
