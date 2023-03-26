package jason.infra.local;

import java.util.logging.Level;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServices;


/**
 * This class implements the Local version of the environment infrastructure tier.
 */
public class LocalEnvironment implements EnvironmentInfraTier {

    /** the user customisation class for the environment */
    private Environment userEnv;
    private BaseLocalMAS masRunner = BaseLocalMAS.getRunner();
    private boolean running = true;

    private static Logger logger = Logger.getLogger(LocalEnvironment.class.getName());

    public LocalEnvironment(ClassParameters userEnvArgs, BaseLocalMAS masRunner) throws JasonException {
        this.masRunner = masRunner;
        if (userEnvArgs != null) {
            try {
                userEnv = (Environment) masRunner.getClass().getClassLoader().loadClass(userEnvArgs.getClassName()).getConstructor().newInstance();
                userEnv.setEnvironmentInfraTier(this);
                userEnv.init(userEnvArgs.getParametersArray());
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error in Local MAS environment creation",e);
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
        LocalAgArch ag = masRunner.getAg(agName);
        if (ag != null) // the agent may was killed
            ag.actionExecuted(action);
    }


    public void informAgsEnvironmentChanged(String... agents) {
        if (agents.length == 0) {
            for (LocalAgArch ag: masRunner.getAgs().values()) {
                ag.getTS().getAgArch().wakeUpSense();
            }
        } else {
            for (String agName: agents) {
                LocalAgArch ag = masRunner.getAg(agName);
                if (ag != null) {
                    if (ag instanceof LocalAgArchAsynchronous) {
                        ((LocalAgArchAsynchronous) ag.getTS().getAgArch()).wakeUpSense();
                    } else {
                        ag.wakeUpSense();
                    }
                } else {
                    logger.log(Level.SEVERE, "Error sending message notification: agent " + agName + " does not exist!");
                }
            }

        }
    }

    public RuntimeServices getRuntimeServices() {
        return new LocalRuntimeServices(masRunner);
    }
}
