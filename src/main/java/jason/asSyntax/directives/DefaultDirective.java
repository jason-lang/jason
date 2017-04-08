package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSyntax.Pred;
import jason.asSyntax.parser.as2j;

public abstract class DefaultDirective implements Directive {

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void begin(Pred directive, as2j parser) {
    }

    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        return innerContent;
    }

    public void end(Pred directive, as2j parser) {
    }

}
