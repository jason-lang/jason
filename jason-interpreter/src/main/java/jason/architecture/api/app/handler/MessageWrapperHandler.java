package jason.architecture.api.app.handler;

import jason.architecture.api.app.model.state.MessageWrapper;
import jason.architecture.api.app.model.state.term.TermWrapper;
import jason.architecture.api.infra.JasonUtils;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.Term;
import jason.infra.local.LocalAgArch;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class MessageWrapperHandler {

    private final Agent agent;

    private final Date time;

    public List<MessageWrapper> extractAll() {
        LocalAgArch localAgArch = JasonUtils.getLocalAgArch(this.agent.getTS().getAg());
        Collection<Message> messages = localAgArch.getMBox();
        List<MessageWrapper> messageWrappers = new ArrayList<>();

        for (Message message : messages) {
            messageWrappers.add(this.extract(message));
        }

        return messageWrappers;
    }

    public MessageWrapper extract(Message message) {
        TermWrapperHandler termWrapperHandler = new TermWrapperHandler();

        List<TermWrapper> wrapper = new ArrayList<>();
        termWrapperHandler.extractAll(wrapper, (Term) message.getPropCont(), null);

        return new MessageWrapper(message.getMsgId(), message.getIlForce(), wrapper.get(0), message.getSender(),
                message.getReceiver(), message.getInReplyTo(), this.agent.getTS().getAgArch().getCycleNumber(),
                this.time);
    }

}
