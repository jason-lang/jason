package jason.infra.centralised;

import jason.infra.InfrastructureFactory;
import jason.infra.MASLauncherInfraTier;
import jason.runtime.RuntimeServices;

public class CentralisedFactory implements InfrastructureFactory {

    public MASLauncherInfraTier createMASLauncher() {
        return new CentralisedMASLauncherAnt();
    }

    public RuntimeServices createRuntimeServices() {
        return new CentralisedRuntimeServices(BaseCentralisedMAS.getRunner());
    }

}
