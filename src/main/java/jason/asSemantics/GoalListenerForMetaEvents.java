package jason.asSemantics;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

public class GoalListenerForMetaEvents implements GoalListener {

    private static final long serialVersionUID = 1L;

    private TransitionSystem ts;

    public GoalListenerForMetaEvents(final TransitionSystem ts) {
        this.ts = ts;
    }

    @Override
    public void goalStarted(Event goal) {
        generateGoalStateEvent(goal.getTrigger().getLiteral(), TEType.achieve, GoalStates.pending, null);
    }

    @Override
    public void goalFailed(Trigger goal, Term reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.failed, reason);
    }

    @Override
    public void goalFinished(Trigger goal, GoalStates result) {
        if (result != null)
            generateGoalStateEvent(goal.getLiteral(), goal.getType(), result, null);
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.finished, null);
    }

    @Override
    public void goalExecuting(Trigger goal, Term reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.executing, reason);
    }

    @Override
    public void goalResumed(Trigger goal, Term reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.resumed, reason);
    }

    @Override
    public void goalSuspended(Trigger goal, Term reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.suspended, reason);
    }

    @Override
    public void goalWaiting(Trigger goal, Term reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.waiting, reason);
    }

    private void generateGoalStateEvent(final Literal goal, final TEType type, final GoalStates state, final Term reason) {
        ts.runAtBeginOfNextCycle(() -> {
                Literal newGoal = goal.forceFullLiteralImpl().copy();
                Literal stateAnnot = ASSyntax.createLiteral("state", new Atom(state.toString()));
                stateAnnot.addAnnot( ASSyntax.createStructure("reason", (reason == null ? new StringTermImpl("") : reason)));
                newGoal.addAnnot( stateAnnot );
                Trigger eEnd = new Trigger(TEOperator.goalState, type, newGoal);
                if (ts.getAg().getPL().hasCandidatePlan(eEnd))
                    ts.getC().insertMetaEvent(new Event(eEnd, null));

        });
        ts.getAgArch().wakeUpDeliberate();
    }
}
