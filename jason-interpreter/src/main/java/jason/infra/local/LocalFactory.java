package jason.infra.local;

import jason.infra.InfrastructureFactory;
import jason.infra.MASLauncherInfraTier;
import jason.runtime.RuntimeServices;

public class LocalFactory implements InfrastructureFactory {

    public MASLauncherInfraTier createMASLauncher() {
        return new LocalMASLauncherAnt();
    }

    public RuntimeServices createRuntimeServices() {
        return new LocalRuntimeServices(BaseLocalMAS.getRunner());
    }

}
