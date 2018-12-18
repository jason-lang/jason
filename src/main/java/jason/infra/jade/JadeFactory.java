package jason.infra.jade;

import jason.infra.InfrastructureFactory;
import jason.infra.MASLauncherInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

public class JadeFactory implements InfrastructureFactory {

    public MASLauncherInfraTier createMASLauncher() {
        return new JadeMASLauncherAnt();
    }

    public RuntimeServicesInfraTier createRuntimeServices() {
        return new JadeRuntimeServices(null, null);
    }
}
