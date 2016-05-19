// Internal action code for project book_trading.mas2j

package jadedf;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.infra.jade.*;

import java.util.logging.Logger;

/** 
 * Register a service in the jade DF (available only when the JADE infrastructure is used)
 *
 * This internal action does not replace the services of the agent, but add a new service.
 * 
 * The first argument is the service type and
 * the second is the name (they should be String).
 * 
 * @author jomi
 */
public class register extends DefaultInternalAction {

    private Logger logger = Logger.getLogger("JadeDF.mas2j."+register.class.getName());

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            if (ts.getUserAgArch().getArchInfraTier() instanceof JasonBridgeArch) {
                // get a reference to the jade agent that represents this Jason agent
                JadeAgArch infra = ((JasonBridgeArch)ts.getUserAgArch().getArchInfraTier()).getJadeAg();

                // 0. get arguments from the AgentSpeak code (type and name of the new service)
                StringTerm type = (StringTerm)args[0];
                StringTerm name = (StringTerm)args[1];
                
                // 1. get current services
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.setName(infra.getAID());

                DFAgentDescription list[] = DFService.search( infra, dfd );

                // 2. deregister
                if ( list.length > 0 ) { 
                    DFService.deregister(infra);
                    dfd = list[0]; // the first result 
                }

                // 3. add a new services
                ServiceDescription sd = new ServiceDescription();
                sd.setType(type.getString());
                sd.setName(name.getString());
                dfd.addServices(sd);
                
                // 4. register again
                DFService.register(infra, dfd);
                
                return true;
            } else {
                logger.warning("jadefd.register can be used only with JADE infrastructure. Current arch is "+ts.getUserAgArch().getArchInfraTier().getClass().getName());
            }
        } catch (Exception e) {
            logger.warning("Error in internal action 'jadedf.register'! "+e);
        }
        return false;
    }
}

