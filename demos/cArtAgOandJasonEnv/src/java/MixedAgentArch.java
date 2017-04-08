import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;

public class MixedAgentArch extends AgArch {

    Set<String> jasonEnvActions = new HashSet<String>();

    @Override
    public void init() throws Exception {
        jasonEnvActions.add("a2");
        // add other actions here

        super.init();
    }

    @Override
    public Collection<Literal> perceive() {
        super.perceive(); // run cartago perceive
        return getArchInfraTier().perceive(); // the perceive of centralised arch
    }

    /** Send specific actions to Jason environment */
    @Override
    public void act(ActionExec act) {
        if (jasonEnvActions.contains(act.getActionTerm().getFunctor())) {
            getArchInfraTier().act(act); // uses the centralised ag arch
        } else {
            super.act(act); // uses cartago ag arch
        }
    }
}
