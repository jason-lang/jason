package jason.infra.centralised;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class implements the centralised version of the environment infrastructure tier.
 */
public class CentralisedEnvironment implements EnvironmentInfraTier {

    /** the user customisation class for the environment */
    private Environment userEnv;
    private BaseCentralisedMAS masRunner = BaseCentralisedMAS.getRunner();
    private boolean running = true;

    private static Logger logger = Logger.getLogger(CentralisedEnvironment.class.getName());

    public CentralisedEnvironment(ClassParameters userEnvArgs, BaseCentralisedMAS masRunner) throws JasonException {
        this.masRunner = masRunner;
        if (userEnvArgs != null) {
            try {
                userEnv = (Environment) getClass().getClassLoader().loadClass(userEnvArgs.getClassName()).newInstance();
                userEnv.setEnvironmentInfraTier(this);
                userEnv.init(userEnvArgs.getParametersArray());
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error in Centralised MAS environment creation",e);
                throw new JasonException("The user environment class instantiation '"+userEnvArgs+"' has failed!"+e.getMessage());
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    /** called before the end of MAS execution, it just calls the user environment class stop method. */
    public void stop() {
        running = false;
        userEnv.stop();
    }

    public void setUserEnvironment(Environment env) {
        userEnv = env;
    }
    public Environment getUserEnvironment() {
        return userEnv;
    }

    /** called by the agent infra arch to perform an action in the environment */
    public void act(final String agName, final ActionExec action) {
        if (running) {
            userEnv.scheduleAction(agName, action.getActionTerm(), action);
        }
    }

    public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData) {
        ActionExec action = (ActionExec)infraData;
        action.setResult(success);
        CentralisedAgArch ag = masRunner.getAg(agName);
        if (ag != null) // the agent may was killed
            ag.actionExecuted(action);
    }


    public void informAgsEnvironmentChanged(String... agents) {
        if (agents.length == 0) {
            for (CentralisedAgArch ag: masRunner.getAgs().values()) {
                ag.getTS().getUserAgArch().wakeUpSense();
            }
        } else {
            for (String agName: agents) {
                CentralisedAgArch ag = masRunner.getAg(agName);
                if (ag != null) {
                    if (ag instanceof CentralisedAgArchAsynchronous) {
                        ((CentralisedAgArchAsynchronous) ag.getTS().getUserAgArch()).wakeUpSense();
                    } else {
                        ag.wakeUpSense();
                    }
                } else {
                    logger.log(Level.SEVERE, "Error sending message notification: agent " + agName + " does not exist!");
                }
            }

        }
    }

    public void informAgsEnvironmentChanged(Collection<String> agentsToNotify) {
        if (agentsToNotify == null) {
            informAgsEnvironmentChanged();
        } else {
            for (String agName: agentsToNotify) {
                CentralisedAgArch ag = masRunner.getAg(agName);
                if (ag != null) {
                    ag.getTS().getUserAgArch().wakeUpSense();
                } else {
                    logger.log(Level.SEVERE, "Error sending message notification: agent " + agName + " does not exist!");
                }
            }
        }
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(masRunner);
    }
}
