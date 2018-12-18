package jason.infra.jade;

import jason.infra.InfrastructureFactory;
import jason.infra.MASLauncherInfraTier;
import jason.runtime.RuntimeServices;

public class JadeFactory implements InfrastructureFactory {

    public MASLauncherInfraTier createMASLauncher() {
        return new JadeMASLauncherAnt();
    }

    public RuntimeServices createRuntimeServices() {
        return new JadeRuntimeServices(null, null);
    }
}
