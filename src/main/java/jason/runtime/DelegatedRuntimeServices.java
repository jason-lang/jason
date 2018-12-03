package jason.runtime;

import java.util.Collection;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.ClassParameters;

public class DelegatedRuntimeServices implements RuntimeServices {

    private RuntimeServices delegate = null;
    
    public DelegatedRuntimeServices(RuntimeServices delegate) {
        this.delegate  = delegate;
    }

    @Override
    public void registerDefaultAgArch(String agArch) {
        delegate.registerDefaultAgArch(agArch);
    }

    public String getNewAgentName(String baseName) {
        return delegate.getNewAgentName(baseName);
    }
    
    public String createAgent(String agName, String agSource, String agClass, Collection<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception {
        return delegate.createAgent(agName, agSource, agClass, archClasses, bbPars, stts, father);
    }

    public void startAgent(String agName) {
        delegate.startAgent(agName);
    }

    public AgArch clone(Agent source, Collection<String> archClasses, String agName) throws JasonException {
        return delegate.clone(source, archClasses, agName);
    }

    public Collection<String> getAgentsNames() {
        return delegate.getAgentsNames();
    }

    public int getAgentsQty() {
        return delegate.getAgentsQty();
    }

    public boolean killAgent(String agName, String byAg) {
        return delegate.killAgent(agName, byAg);
    }

    public void stopMAS() throws Exception {
        delegate.stopMAS();
    }

    @Override    
    public void dfRegister(String agName, String service, String type) {
        delegate.dfRegister(agName, service, type);
    }

    @Override    
    public void dfDeRegister(String agName, String service, String type) {
        delegate.dfDeRegister(agName, service, type);
    }
    
    @Override    
    public Collection<String> dfSearch(String service, String type) {
        return delegate.dfSearch(service, type);
    }
    
    @Override    
    public void dfSubscribe(String agName, String service, String type) {
        delegate.dfSubscribe(agName, service, type);
    }
    
}

