package jason.infra.centralised;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.control.ExecutionControl;
import jason.control.ExecutionControlInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

/**
 * Concrete implementation of the controller for centralised infrastructure
 * tier.
 */
public class CentralisedExecutionControl implements ExecutionControlInfraTier {

    private ExecutionControl userController;

    private BaseCentralisedMAS masRunner = null;

    private static Logger logger = Logger.getLogger(CentralisedExecutionControl.class.getName());

    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    public CentralisedExecutionControl(ClassParameters userControlClass, BaseCentralisedMAS masRunner) throws JasonException {
        this.masRunner = masRunner;
        try {
            userController = (ExecutionControl) Class.forName(userControlClass.getClassName()).newInstance();
            userController.setExecutionControlInfraTier(this);
            userController.init(userControlClass.getParametersArray());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error ", e);
            throw new JasonException("The user execution control class instantiation '" + userControlClass + "' has failed!" + e.getMessage());
        }
    }

    public void stop() {
        userController.stop();
    }

    public ExecutionControl getUserControl() {
        return userController;
    }

    public void receiveFinishedCycle(String agName, boolean breakpoint, int cycle) {
        // pass to user controller
        userController.receiveFinishedCycle(agName, breakpoint, cycle);
    }

    public void informAgToPerformCycle(String agName, int cycle) {
        // call the agent method to "go on"
        CentralisedAgArch infraArch = masRunner.getAg(agName);
        AgArch arch = infraArch.getUserAgArch();
        arch.setCycleNumber(cycle);
        infraArch.receiveSyncSignal();
    }

    public void informAllAgsToPerformCycle(final int cycle) {
        executor.execute(new Runnable() {
            public void run() {
                synchronized (masRunner.getAgs()) {
                    for (CentralisedAgArch ag: masRunner.getAgs().values()) {
                        ag.getUserAgArch().setCycleNumber(cycle);
                        ag.receiveSyncSignal();
                    }
                }
            }
        });
    }

    public Document getAgState(String agName) {
        AgArch arch = masRunner.getAg(agName).getUserAgArch();
        if (arch != null) { // the agent exists ?
            return arch.getTS().getAg().getAgState();
        } else {
            return null;
        }
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(masRunner);
    }
}
