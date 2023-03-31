package jason.infra.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jason.runtime.RuntimeServices;
/** implement services that are based on BaseLocalMAS */
public abstract class BaseRuntimeServices implements RuntimeServices {

    protected BaseLocalMAS masRunner;
    private Collection<String> defaultAgArchs = new ArrayList<>();

    public BaseRuntimeServices(BaseLocalMAS masRunner) {
        this.masRunner = masRunner;
    }

    /** Every agent that will be create use registered archs */
    @Override
    public void registerDefaultAgArch(String agArch) {
        defaultAgArchs.add(agArch);
    }
    @Override
    public Collection<String> getDefaultAgArchs() {
        return defaultAgArchs;
    }

    public String getNewAgentName(String baseName) {
        int n = 1;
        String nb = "";
        while (masRunner.getAg(baseName+nb) != null)
            nb = "_" + (n++);
        return baseName + nb;
    }


    public Collection<String> getAgentsNames() {
        return masRunner.getAgs().keySet();
    }

    public int getAgentsQty() {
        return getAgentsNames().size();
    }

    public void stopMAS(int deadline, boolean stopJVM, int exitValue) throws Exception {
        masRunner.finish(deadline, stopJVM, exitValue);
    }

    @Override
    public void dfRegister(String agName, String service, String type) {
        masRunner.dfRegister(agName, service);
    }

    @Override
    public void dfDeRegister(String agName, String service, String type) {
        masRunner.dfDeRegister(agName, service);
    }

    @Override
    public Collection<String> dfSearch(String service, String type) {
        return masRunner.dfSearch(service);
    }

    @Override
    public void dfSubscribe(String agName, String service, String type) {
        masRunner.dfSubscribe(agName, service);
    }

    @Override
    public Map<String, Set<String>> getDF() {
        return masRunner.getDF();
    }

    @Override
    public Map<String, Map<String, Object>> getWP() {
        return masRunner.getWP();
    }
}

