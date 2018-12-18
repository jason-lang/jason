package jason.infra;

import jason.runtime.RuntimeServicesInfraTier;

/**
 * Every infrastructure for Jason must implement this interface.  The
 * interface provides methods for JasonIDE and user's runtime classes,
 * etc.
 *
 * @author Jomi
 */
public interface InfrastructureFactory {

    /**
     * Every infrastructure factory should be able to create an
     * instance of MASLauncherInfraTier, this instance is used to
     * start a new MAS execution. It is normally used by JasonIDE.
     */
    public MASLauncherInfraTier createMASLauncher();

    /**
     * Every infrastructure factory should be able to create an
     * instance of RuntimeServicesInfraTier, this instance provides
     * services for controlling the MAS, as agent creation,
     * destruction, etc. These services are normally used by user
     * classes as AgArch and Environments.
     */
    public RuntimeServicesInfraTier createRuntimeServices();
}
