import jason.architecture.AgArch;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.GoalListener;
import jason.asSemantics.Intention;
import jason.asSyntax.Trigger;

@SuppressWarnings("serial")
public class MyArch extends AgArch {

    int nbGoal = 0;
    int nbIntentions = 0;
    
    @Override
    public void init() throws Exception {
        getTS().addGoalListener(new GoalListener() {
            @Override
            public void goalFinished(Trigger goal, GoalStates result) {
                nbGoal++;
                getTS().getLogger().info("goal finished: "+goal.getLiteral()+", total="+nbGoal);
            }
        });
        
        getTS().getC().addEventListener(new CircumstanceListener() {
            @Override
            public void intentionDropped(Intention i) {
                nbIntentions++;
                getTS().getLogger().info("intention finished: "+i.getId()+", total="+nbIntentions);
            }
        });
    }
}
