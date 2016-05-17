//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

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

    private RunCentralisedMAS masRunner = null;

    private static Logger logger = Logger.getLogger(CentralisedExecutionControl.class.getName());

    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public CentralisedExecutionControl(ClassParameters userControlClass, RunCentralisedMAS masRunner) throws JasonException {
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
