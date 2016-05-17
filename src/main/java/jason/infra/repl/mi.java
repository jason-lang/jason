package jason.infra.repl;

import jason.architecture.MindInspectorWeb;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.awt.Desktop;
import java.net.URI;

public class mi extends DefaultInternalAction {

    @Override public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String url = MindInspectorWeb.getURL()+"/agent-mind/"+ts.getUserAgArch().getAgName();
        Desktop.getDesktop().browse(new URI(url));
        return true;
    }
    
}
