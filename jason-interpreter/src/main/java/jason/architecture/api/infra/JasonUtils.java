package jason.architecture.api.infra;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.infra.local.BaseLocalMAS;
import jason.infra.local.LocalAgArch;

public class JasonUtils {

    public static LocalAgArch getLocalAgArch(Agent agent) {
        LocalAgArch localAgArch = null;
        AgArch agArch = agent.getTS().getAgArch();
        while (agArch != null) {
            if (agArch instanceof LocalAgArch) {
                localAgArch = (LocalAgArch) agArch;
                break;
            }
            agArch = agArch.getNextAgArch();
        }
        return localAgArch;
    }

    public static Agent getAgentFromSMA(String name) {
        LocalAgArch cag = BaseLocalMAS.getRunner().getAg(name);
        if (cag != null) {
            return cag.getTS().getAg();
        } else {
            return null;
        }
    }

    public static String getMasName() {
        return BaseLocalMAS.getRunner().getProject().getSocName();
    }

}
