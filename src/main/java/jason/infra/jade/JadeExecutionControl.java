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

package jason.infra.jade;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
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
 * Concrete execution control implementation for Jade infrastructure.
 */
@SuppressWarnings("serial")
public class JadeExecutionControl extends JadeAg implements ExecutionControlInfraTier {

    public static String controllerOntology = "AS-ExecControl";

    private ExecutionControl userControl;
    private ExecutorService executor; // the thread pool used to execute actions
    
    @Override
    public void setup()  {
        logger = Logger.getLogger(JadeExecutionControl.class.getName());
        
        // create the user environment
        try {
            Object[] args = getArguments();
            if (args != null && args.length > 0) {
                if (args[0] instanceof ClassParameters) { // it is an mas2j parameter
                    ClassParameters ecp = (ClassParameters)args[0];
                    userControl = (ExecutionControl) Class.forName(ecp.getClassName()).newInstance();
                    userControl.setExecutionControlInfraTier(this);
                    userControl.init(ecp.getParametersArray());
                } else {
                    userControl = (ExecutionControl) Class.forName(args[0].toString()).newInstance();
                    userControl.setExecutionControlInfraTier(this);
                    if (args.length > 1) {
                        logger.warning("Execution control arguments is not implemented yet (ask it to us if you need)!");
                    }
                }
            } else {
                logger.warning("Using default execution control.");
                userControl = new ExecutionControl();
                userControl.setExecutionControlInfraTier(this);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in setup Jade Environment", e);
        }

        executor = Executors.newFixedThreadPool(5);
        
        try {
            addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    userControl.updateNumberOfAgents();
                    informAllAgsToPerformCycle(0);
                    /*
                    executor.execute(new Runnable() {
                        public void run() {
                        }
                    });
                    */
                }
            });

            addBehaviour(new CyclicBehaviour() {
                ACLMessage m;
                public void action() {
                    m = receive();
                    if (m == null) {
                        block(1000);
                    } else {
                        try {
                            // check if the message is an agent state
                            @SuppressWarnings("unused")
                            Document o = (Document)m.getContentObject();
                            logger.warning("Received agState too late! in-reply-to:"+m.getInReplyTo());
                        } catch (Exception _) {
                            try {
                                // check if the message is an end of cycle from some agent 
                                final String content = m.getContent();
                                final int p = content.indexOf(",");
                                if (p > 0) {
                                    final String sender  = m.getSender().getLocalName();
                                    final boolean breakpoint = Boolean.parseBoolean(content.substring(0,p));
                                    final int cycle = Integer.parseInt(content.substring(p+1));
                                    executor.execute(new Runnable() {
                                        public void run() {
                                            try {
                                                userControl.receiveFinishedCycle(sender, breakpoint, cycle);
                                            } catch (Exception e) {
                                                logger.log(Level.SEVERE, "Error processing end of cycle.", e);                                              
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Error in processing "+m, e);
                            }
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agent", e);
        }
    }

    @Override
    protected void takeDown() {
        if (userControl != null) userControl.stop();
    }
    
    public ExecutionControl getUserControl() {
        return userControl;
    }
    
    public void informAgToPerformCycle(final String agName, final int cycle) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                m.setOntology(controllerOntology);
                m.addReceiver(new AID(agName, AID.ISLOCALNAME));
                m.setContent("performCycle");
                m.addUserDefinedParameter("cycle", String.valueOf(cycle));
                send(m);
            }
        });
    }

    public void informAllAgsToPerformCycle(final int cycle) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                try {
                    logger.fine("Sending performCycle "+cycle+" to all agents.");
                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                    m.setOntology(controllerOntology);
                    addAllAgsAsReceivers(m);
                    m.setContent("performCycle");
                    m.addUserDefinedParameter("cycle", String.valueOf(cycle));
                    send(m);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error in informAllAgsToPerformCycle", e);
                }
            }
        });
    }

    public Document getAgState(final String agName) {
        if (agName == null) return null;
        
        state = null;
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                try {
                    ACLMessage m = new ACLMessage(ACLMessage.QUERY_REF);
                    m.setOntology(controllerOntology);
                    m.addReceiver(new AID(agName, AID.ISLOCALNAME));
                    m.setContent("agState");
                    ACLMessage r = ask(m);
                    if (r == null) {
                        System.err.println("No agent state received! (possibly timeout in ask)");
                    } else {
                        state = (Document) r.getContentObject();
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error in getAgState", e);
                } finally {
                    synchronized (syncWaitState) {
                        syncWaitState.notifyAll();
                    }
                }
            }
        });
        return waitState();
    }
    
    private Document state = null;
    private Object syncWaitState = new Object();
    private Document waitState() {
        if (state == null) {
            synchronized (syncWaitState) {
                try {
                    syncWaitState.wait();
                } catch (InterruptedException e) {}
            }
        }
        return state;
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new JadeRuntimeServices(getContainerController(), this);
    }
}
