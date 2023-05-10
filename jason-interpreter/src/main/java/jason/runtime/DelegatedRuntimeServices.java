package jason.runtime;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.ClassParameters;

public class DelegatedRuntimeServices implements RuntimeServices {

    private RuntimeServices delegate = null;

    public DelegatedRuntimeServices(RuntimeServices delegate) {
        this.delegate  = delegate;
    }

    public void registerDefaultAgArch(String agArch) throws RemoteException {
        delegate.registerDefaultAgArch(agArch);
    }
    public Collection<String> getDefaultAgArchs() throws RemoteException {
        return delegate.getDefaultAgArchs();
    }

    public String getNewAgentName(String baseName) throws RemoteException {
        return delegate.getNewAgentName(baseName);
    }

    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception, RemoteException {
        return delegate.createAgent(agName, agSource, agClass, archClasses, bbPars, stts, father);
    }

    public boolean isRunning() throws RemoteException {
        return delegate.isRunning();
    }
    public void startAgent(String agName) throws RemoteException {
        delegate.startAgent(agName);
    }

    public void clone(Agent source, List<String> archClasses, String agName) throws RemoteException, JasonException {
        delegate.clone(source, archClasses, agName);
    }

    public Collection<String> getAgentsNames() throws RemoteException {
        return delegate.getAgentsNames();
    }

    public int getAgentsQty() throws RemoteException {
        return delegate.getAgentsQty();
    }

    @Override
    public Agent getAgentSnapshot(String agName) throws RemoteException {
        return delegate.getAgentSnapshot(agName);
    }

    @Override
    public String getMASName() throws RemoteException {
        return delegate.getMASName();
    }

    @Override
    public String loadASL(String agName, String code, String sourceId, boolean replace) throws RemoteException {
        return delegate.loadASL(agName, code, sourceId, replace);
    }

    public boolean killAgent(String agName, String byAg, int deadline) throws RemoteException {
        return delegate.killAgent(agName, byAg, deadline);
    }

    public void stopMAS(int deadline, boolean stopJVM, int exitValue) throws RemoteException, Exception {
        delegate.stopMAS(deadline, stopJVM, exitValue);
    }

    @Override
    public void dfRegister(String agName, String service, String type) throws RemoteException {
        delegate.dfRegister(agName, service, type);
    }

    @Override
    public void dfDeRegister(String agName, String service, String type) throws RemoteException {
        delegate.dfDeRegister(agName, service, type);
    }

    @Override
    public Collection<String> dfSearch(String service, String type) throws RemoteException {
        return delegate.dfSearch(service, type);
    }

    @Override
    public void dfSubscribe(String agName, String service, String type) throws RemoteException {
        delegate.dfSubscribe(agName, service, type);
    }

    @Override
    public Map<String, Set<String>> getDF() throws RemoteException {
        return delegate.getDF();
    }

    @Override
    public Map<String, Map<String, Object>> getWP() throws RemoteException {
        return delegate.getWP();
    }
}

