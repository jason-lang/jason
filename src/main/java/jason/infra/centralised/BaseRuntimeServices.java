package jason.infra.centralised;

import java.util.ArrayList;
import java.util.Collection;

import jason.runtime.RuntimeServices;
/** implement services that are based on BaseCentralisedMAS */
public abstract class BaseRuntimeServices implements RuntimeServices {

    protected BaseCentralisedMAS masRunner;
    protected Collection<String> defaultAgArchs = new ArrayList<>();
    
    public BaseRuntimeServices(BaseCentralisedMAS masRunner) {
        this.masRunner = masRunner;
    }

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
        return masRunner.getAgs().keySet().size();
    }

    public void stopMAS() throws Exception {
        masRunner.finish();
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
    
}

