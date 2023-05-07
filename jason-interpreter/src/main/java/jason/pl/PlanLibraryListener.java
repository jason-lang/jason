package jason.pl;

import jason.asSyntax.Plan;

import java.io.Serializable;

/** call-back interface to be notified about events on plabs */
public interface PlanLibraryListener extends Serializable {

    public void planAdded(Plan p);
    public void planRemoved(Plan p);

}
